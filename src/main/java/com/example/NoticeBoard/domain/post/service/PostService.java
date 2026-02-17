package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, PostEvent> kafkaTemplate;
    private final PostSearchRepository postSearchRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOPIC = "post-events";

    private Pageable createPageable(int page, int size){
        if(page < 0 || size <= 0){
            throw new IllegalArgumentException("잘못된 페이지 요청입니다.");
        }

        return PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "createAt"));
    }

    // kafka는 데이터의 상태가 변하는 시점에 사용됨 -> CUD(Create, Update, Delete)로직이 완료된 직후에 호출
    // 데이터 동기화가 필요한 메소드에 주로 들어감. 단순 조회(Read) 메소드엔 들어가지 않음.
    // DB와 검색 엔진(ES: Elasticsearch)사이의 결합도를 낮추기 위함. DB저장은 성공했는데 ES 저장이 실패할 경우, Kafka가 중간에서 이벤트를 보관해주어 나중에라도 재처리할 수 있게 도와줌.
    // 결합도를 낮추면 응집도가 높아지고, 그러면 모듈의 독립성을 높여 유지보수와 재사용을 극대화 할 수 있고, 내부 요소들끼리 더 밀접하게 연관되어 시스템이 안정적이고 수정이 용이해진다.

    // 게시글 작성 (나중에 예외 처리를 RuntimeException, IllegalArgumentException 말고 자세히 할 필요가 있음)
    public PostResponseDto createPost(Long userId, PostRequestDto requestDto){
        if(!userRepository.existsById(userId)){
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        Post post = Post.builder()
                .category(requestDto.getCategory())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .imageUri(requestDto.getImageUri())
                .fileUri(requestDto.getFileUri())
                .userId(userId)
                .postStatus(PostStatus.NORMAL)
                .likeCount(0)
                .build();

        Post savedPost = postRepository.save(post);
        PostResponseDto response = PostResponseDto.fromEntity(savedPost);

        // Kafka 이벤트 생성
        sendEvent(savedPost.getId(), "CREATE", response);

        return response;
    }

    // 게시글 수정 - 본인이 작성한 게시글 일때만 수정 버튼 생성 및 수정 가능
    public PostResponseDto updatePost(Long postId, Long userId, PostRequestDto requestDto){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 삭제된 게시글인지 확인
        // 왜 == 으로 했냐? equals가 아니라 => JVM에서는 enum은 싱글톤 인스턴스로 관리됨. 따라서 같은 객체인지 비교(reference 비교)하는 == 이 더 적합함. 값을 비교하는 equals 는 x.
        // "equals"를 쓰면 PostStatus가 null 이면 예외 발생가 발생하지만, "=="으로 사용하면 false를 반환함.
        if (post.getPostStatus() == PostStatus.DELETED) {
            throw new IllegalArgumentException("삭제된 게시글은 수정할 수 없습니다.");
        }

        post.setCategory(requestDto.getCategory());
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        if (requestDto.getImageUri() != null) {
            post.setImageUri(requestDto.getImageUri());
        }

        if (requestDto.getFileUri() != null) {
            post.setFileUri(requestDto.getFileUri());
        }

        PostResponseDto response = PostResponseDto.fromEntity(post);

        // Kafka 이벤트 생성
        sendEvent(post.getId(), "UPDATE", response);

        // Redis 기존 캐시 삭제
        evictPostCache(postId);

        return response;
    }

    // 게시글 삭제 (Soft Delete)
    public void deletePost(Long postId, Long userId){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        post.setDeletedAt(LocalDateTime.now());
        post.setPostStatus(PostStatus.DELETED);

        // Kafka 이벤트 생성
        sendEvent(postId, "DELETE", null);

        // Redis 캐시 삭제
        evictPostCache(postId);
    }

    // ------------------------------------- 여기서 부터 수정 필요---------------

    // 게시글 조회(전체) - 실무에서는 findAll()를 사용하지 않음
    // -> 대규모 서비스에서는 데이터의 양이 만약 10만건이 들어온다고 하면 해당 데이터를 전부 찾는데 많은 시간이 소요되고 GC 압박(cpu 자원을 과도하게 소모하고 프로그램 성능을 저하시키는 상태)와 OutOfMemory 발생 가능.
    // 따라서 페이징을 사용해서 한 페이지에 나오는 수 만큼만 찾음. (1~20)
    public Page<PostResponseDto> findAllPosts(int page, int size){
        return postRepository.findAllPosts(createPageable(page, size));
    }

    // 게시글 조회(제목)
    public Page<PostResponseDto> findByTitle(String keyword, int page, int size){
        return postRepository.findByTitle(keyword, createPageable(page, size));
    }

    // 게시글 조회(내용)
    public Page<PostResponseDto> findByContent(String keyword, int page, int size){
        return postRepository.findByContent(keyword, createPageable(page, size));
    }

    // 게시글 조회(작성자)
    public Page<PostResponseDto> findByNickname(String keyword, int page, int size){
        return postRepository.findByNickname(keyword, createPageable(page, size));
    }

    // 게시글 조회(제목 + 내용)
    public Page<PostResponseDto> findByTitleAndContent(String keyword, int page, int size){
        return postRepository.findByTitleAndContent(keyword, createPageable(page, size));
    }

    private void sendEvent(Long postId, String type, PostResponseDto postResponseDto){
        PostEvent event = new PostEvent(postId, type, postResponseDto);
        kafkaTemplate.send(TOPIC, String.valueOf(postId), event)
                .addCallback(
                        result -> log.info("Kafka 전송 성공: {}", postId),
                        ex -> log.error("Kafka 전송 실패: {}", ex.getMessage())
                );
    }

    // Redis 캐시 삭제 - 데이터가 수정되거나 삭제되면 캐시를 제거해서 데이터 불일치를 방지시킴.
    private void evictPostCache(Long postId){
        redisTemplate.delete("post:detail:" + postId);
    }

//    // 게시글 좋아요
//    public void likePost(Long postId, Long userId) {
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
//
//        if(!userRepository.existsById(userId)){
//            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
//        }
//
//        PostLike postLike = PostLike.builder()
//                .postId(postId)
//                .userId(userId)
//                .build();
//
//        postLikeRepository.save(postLike);
//
//        post.setLikeCount(post.getLikeCount() + 1);
//    }
//
//    // 게시글 좋아요 취소
//    public void unlikePost(Long postId, Long userId) {
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
//
//        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
//                .orElseThrow(() -> new IllegalStateException("좋아요 기록이 없습니다."));
//
//        postLikeRepository.delete(postLike);
//
//        post.setLikeCount(post.getLikeCount() - 1);
//    }


//    // 게시글 신고
//    public void reportPost(Long postId, Long userId, PostReportRequestDto requestDto) {
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
//
//        if (postReportRepository.existsByPostIdAndUserId(postId, userId)) {
//            throw new IllegalStateException("이미 신고한 게시글입니다.");
//        }
//
//        if (post.getUserId().equals(userId)) {
//            throw new IllegalArgumentException("본인이 작성한 게시글은 신고할 수 없습니다.");
//        }
//
//        PostReport report = PostReport.builder()
//                .post(post)
//                .user(user)
//                .content(requestDto.getContent())
//                .reportReason(requestDto.getReason())
//                .reportStatus(ReportStatus.PROCESSING)
//                .build();
//
//        postReportRepository.save(report);
//
//        // 신고 5회 이상 → 자동 블라인드
//        if (post.getReports().size() >= 5) {
//            post.setPostStatus(PostStatus.BLIND);
//        }
//    }

}
