package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.dto.PostSearchResponseDto;
import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.post.repository.PostSearchRepository;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

// Post 읽기 전용 서비스
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostQueryService {
    private final PostRepository postRepository;
    private final PostSearchRepository postSearchRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // Pageable 생성 헬퍼 메소드
    private Pageable createPageable(int page, int size){
        if(page < 0 || size <= 0){
            throw new IllegalArgumentException("잘못된 페이지 요청입니다.");
        }

        return PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
