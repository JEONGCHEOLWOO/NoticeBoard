package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.dto.PostSearchResponseDto;
import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.event.ViewCountProducer;
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
    private final ViewCountProducer viewCountProducer;

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
        // GET post:detail:postId -> GET post:detail:12 -> 게시글 12의 값을 불러옴
        // 여기서 Redis의 String 명령어 Get 실행.
        PostResponseDto cachedPost = (PostResponseDto) redisTemplate.opsForValue().get(cacheKey);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));

        // 삭제된 게시글 확인
        if (post.isDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글입니다.");
        }

        if(cachedPost != null){
            log.info("Redis 캐시 히트: postId={}", postId);
            return cachedPost;
        }

        log.info("Redis 캐시 미스: DB 조회 진행 postId={}", postId);

        PostResponseDto response = PostResponseDto.fromEntity(post);

        // Redis에 캐시 저장
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(10));

        log.info("게시글 상세 조회 완료: postId={}", postId);

        return response;
    }

    // 게시글 조회(제목, 내용, 제목+내용, 작성자)
    // @Transactional(readOnly = true) 는 읽기 전용 모드로 성능 향상에 도움이 된다. 해당 속성을 true로 설정함으로 트랜잭션이 데이터 베이스에 대한 변경을 수행하지 않도록해서 데이터의 무결성을 보장하는데 도움이 된다.
    // 위 어노테이션은 대표적으로 SimpleJpaRepository에 있는 findById, save, delete 메소드에 구현되어 있다.
    // 찾아보니 확실히 성능 개선에 대해서는 이점이 있지만, 추가 쿼리로 인해 DB의 네트워크 요청 건수 또한 최대 6배까지 늘어날 수 있어 비용이 많이 들 수 있기 때문에, 단건 조회(update, insert)요청 메소드에서는 사용하지 않는 것을 추천한다고 한다.
    @Transactional(readOnly = true)
    public Page<PostSearchResponseDto> searchPosts(String keyword, String type, Pageable pageable){
        
        Page<PostSearchDocument> documents = postSearchRepository.searchByCondition(keyword, type, pageable);
        
        log.info("게시글 검색 완료: keyword={}, 결과 개수={}, totalElements={}", keyword, documents.getNumberOfElements(), documents.getTotalElements());
        
        return documents.map(PostSearchResponseDto::fromEntity);
    }

    // 게시글 조회(전체) - 실무에서는 findAll()를 사용하지 않음
    // -> 대규모 서비스에서는 데이터의 양이 만약 10만건이 들어온다고 하면 해당 데이터를 전부 찾는데 많은 시간이 소요되고 GC 압박(cpu 자원을 과도하게 소모하고 프로그램 성능을 저하시키는 상태)와 OutOfMemory 발생 가능.
    // 따라서 페이징을 사용해서 한 페이지에 나오는 수 만큼만 찾음. (1~20)
    @Transactional(readOnly = true)
    public Page<PostSearchResponseDto> findAllPosts(int page, int size){
        Pageable pageable = createPageable(page,size);

        Page<Post> posts = postRepository.findByPostStatus(PostStatus.NORMAL, pageable);

        log.info("전체 게시글 조회 완료: page={}, 조회 개수={}, totalElements={}", page, posts.getNumberOfElements(), posts.getTotalElements());

        return posts.map(post -> PostSearchResponseDto.builder()
                .id(post.getId())
                .postId(post.getId())
                .userId(post.getUserId())
                .category(post.getCategory())
                .title(post.getTitle())
                .image(post.getImageUrl() != null)
                .postStatus(post.getPostStatus())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .nickname(post.getNickname())
                .createdAt(post.getCreatedAt())
                .build());
    }

    // 조회수 증가 - kafka + redis 이용
    public void incrementViewCount(Long postId) {
        viewCountProducer.sendViewEvent(postId);
        log.info("게시글 조회수 증가 완료: postId={}", postId);
    }
}
