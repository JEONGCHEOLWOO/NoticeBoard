package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.dto.PostSearchResponseDto;
import com.example.NoticeBoard.domain.post.entity.PostEvent;
import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.repository.PostSearchRepository;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, PostEvent> kafkaTemplate;
    private final PostSearchRepository postSearchRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOPIC = "post-events";

    // Pageable 생성 헬퍼 메소드
    private Pageable createPageable(int page, int size){
        if(page < 0 || size <= 0){
            throw new IllegalArgumentException("잘못된 페이지 요청입니다.");
        }

        return PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
        sendEvent(savedPost.getId(), "CREATE");

        log.info("게시글 생성 완료: postId={}, userId={}", savedPost.getId(), userId);

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

        // 비즈니스 메소드를 해야될까?
        // 도메인 주도 설계를 하게 되면, 서비스는 엔터티를 호출하는 정도의 얇은 비즈니스 로직을 가지게 됨.
        // 하지만 엔터티를 단순히 데이터를 전달하는 역할로 사용하고, 서비스에 비즈니스 로직을 두어도 된다.
        // 엔터티에 비즈니스 로직을 설계하면 되는 경우 객체로 사용하는 것이고, 서비스에 비즈니스 로직을 설계하면 엔터티는 자료구조로 사용하는 방식이 된다.
        // 둘중 옳은 방법은 없다. 프로젝트에 맞는 방식으로 설계를 하면 된다.
        // 그러면 내 현재 설계는 도메인 주도 설계 -> MSA 설계로 넘거갈 예정이니 도메인 주도 설계에 초점을 두어 비즈니스 메소드를 엔터티에 두는것이 좋을꺼 같다.
        post.update(requestDto.getTitle(), requestDto.getContent(), requestDto.getCategory(), requestDto.getPostStatus());

        if (requestDto.getImageUri() != null) {
            post.setImageUri(requestDto.getImageUri());
        }

        if (requestDto.getFileUri() != null) {
            post.setFileUri(requestDto.getFileUri());
        }

        PostResponseDto response = PostResponseDto.fromEntity(post);

        // Kafka 이벤트 생성
        sendEvent(post.getId(), "UPDATE");

        // Redis 기존 캐시 삭제
        evictPostCache(postId);

        log.info("게시글 수정 완료: postId={}, userId={}", postId, userId);

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

        post.delete();

        // Kafka 이벤트 생성
        sendEvent(postId, "DELETE");

        // Redis 캐시 삭제
        evictPostCache(postId);

        log.info("게시글 삭제 완료: postId={}, userId={}", postId, userId);
    }

    // 게시글 조회 (내용)
    @Transactional(readOnly = true)
    public PostResponseDto getPostDetail(Long postId){
        String cacheKey = "post:detail:" + postId;
        PostResponseDto cachedPost = (PostResponseDto) redisTemplate.opsForValue().get(cacheKey);

//        캐시 히트(Cache Hit)
//        캐시 히트는 CPU가 필요한 데이터가 캐시에 이미 존재하는 경우를 의미
//        이 경우, 데이터는 캐시에서 즉시 액세스되며 메인 메모리로 접근할 필요가 없음
//        결과적으로 데이터 접근 시간이 매우 짧아서 시스템의 전체 성능이 향상
//        캐시 미스(Cache Miss)
//        캐시 미스는 CPU가 필요한 데이터가 캐시에 존재하지 않는 경우를 의미
//        이 경우, 데이터는 메인 메모리에서 가져와야 하며 이 과정에서 캐시에도 해당 데이터가 저장
//        캐시 미스는 데이터 접근 시간이 길어져 성능 저하를 초래할 수 있음
        if(cachedPost != null){
            log.info("Redis 캐시 히트: postId={}", postId);
            return cachedPost;
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 삭제된 게시글 확인
        if (post.isDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        PostResponseDto response = PostResponseDto.fromEntity(post);

        // Redis에 캐시 저장
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(10));

        log.info("게시글 조회 완료: postId={}", postId);

        return response;
    }

    // 게시글 조회(제목, 내용, 제목+내용, 작성자)
    // @Transactional(readOnly = true) 는 읽기 전용 모드로 성능 향상에 도움이 된다. 해당 속성을 true로 설정함으로 트랜잭션이 데이터 베이스에 대한 변경을 수행하지 않도록해서 데이터의 무결성을 보장하는데 도움이 된다.
    // 위 어노테이션은 대표적으로 SimpleJpaRepository에 있는 findById, save, delete 메소드에 구현되어 있다.
    // 찾아보니 확실히 성능 개선에 대해서는 이점이 있지만, 추가 쿼리로 인해 DB의 네트워크 요청 건수 또한 최대 6배까지 늘어날 수 있어 비용이 많이 들 수 있기 때문에, 단건 조회(update, insert)요청 메소드에서는 사용하지 않는 것을 추천한다고 한다.
    @Transactional(readOnly = true)
    public Page<PostSearchResponseDto> searchPosts(String keyword, String type, Pageable pageable){
        log.info("게시글 검색: keyword={}, type={}", keyword, type);

        Page<PostSearchDocument> documents = postSearchRepository.searchByCondition(keyword, type, pageable);
        return documents.map(PostSearchResponseDto::fromEntity);
    }

    // 게시글 조회(전체) - 실무에서는 findAll()를 사용하지 않음
    // -> 대규모 서비스에서는 데이터의 양이 만약 10만건이 들어온다고 하면 해당 데이터를 전부 찾는데 많은 시간이 소요되고 GC 압박(cpu 자원을 과도하게 소모하고 프로그램 성능을 저하시키는 상태)와 OutOfMemory 발생 가능.
    // 따라서 페이징을 사용해서 한 페이지에 나오는 수 만큼만 찾음. (1~20)
    @Transactional(readOnly = true)
    public Page<PostSearchResponseDto> findAllPosts(int page, int size){
        Pageable pageable = createPageable(page,size);

        Page<Post> posts = postRepository.findByPostStatus(PostStatus.NORMAL, pageable);

        return posts.map(post -> PostSearchResponseDto.builder()
                .id(String.valueOf(post.getId()))
                .postId(post.getId())
                .userId(post.getUserId())
                .category(post.getCategory())
                .title(post.getTitle())
                .image(post.getImageUri() != null)
                .postStatus(post.getPostStatus())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .nickname(post.getNickname())
                .createdAt(post.getCreatedAt())
                .build());
    }


    // 조회수 증가
    public void incrementViewCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.incrementViewCount();
        log.debug("조회수 증가: postId={}, viewCount={}", postId, post.getViewCount());
    }

    private void sendEvent(Long postId, String type){
        PostEvent event = new PostEvent(postId, type);
        kafkaTemplate.send(TOPIC, String.valueOf(postId), event)
                .addCallback(
                        result -> log.info("Kafka 전송 성공: postId={}, type={}", postId, type),
                        ex -> log.error("Kafka 전송 실패: postId={}, type={}, error={}", postId, type, ex.getMessage())
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
