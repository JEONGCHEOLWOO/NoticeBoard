package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.event.PostEventProducer;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.user.entity.User;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Post 쓰기(CUD) 전용 서비스
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostCommandService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostEventProducer postEventProducer;

    public static final String POST_DETAIL = "post:detail:";

    // kafka는 데이터의 상태가 변하는 시점에 사용됨 -> CUD(Create, Update, Delete)로직이 완료된 직후에 호출
    // 데이터 동기화가 필요한 메소드에 주로 들어감. 단순 조회(Read) 메소드엔 들어가지 않음.
    // DB와 검색 엔진(ES: Elasticsearch)사이의 결합도를 낮추기 위함. DB저장은 성공했는데 ES 저장이 실패할 경우, Kafka가 중간에서 이벤트를 보관해주어 나중에라도 재처리할 수 있게 도와줌.
    // 결합도를 낮추면 응집도가 높아지고, 그러면 모듈의 독립성을 높여 유지보수와 재사용을 극대화 할 수 있고, 내부 요소들끼리 더 밀접하게 연관되어 시스템이 안정적이고 수정이 용이해진다.

    // 게시글 작성 (나중에 예외 처리를 RuntimeException, IllegalArgumentException 말고 자세히 할 필요가 있음)
    public PostResponseDto createPost(Long userId, PostRequestDto postRequestDto){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        Post post = Post.builder()
                .category(postRequestDto.getCategory())
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .imageUrl(postRequestDto.getImageUrl())
                .fileUrl(postRequestDto.getFileUrl())
                .userId(userId)
                .postStatus(PostStatus.NORMAL)
                .nickname(user.getNickname())
                .viewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .build();

        Post savedPost = postRepository.save(post);

        postEventProducer.sendPostCreatedEvent(savedPost.getId());
        log.info("게시글 생성 완료: postId={}, userId={}", savedPost.getId(), userId);

        return PostResponseDto.fromEntity(savedPost);
    }

    // 게시글 수정 - 본인이 작성한 게시글 일때만 수정 버튼 생성 및 수정 가능
    public PostResponseDto updatePost(Long postId, Long userId, PostRequestDto postRequestDto){

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 삭제된 게시글인지 확인
        if (post.isDeleted()) {
            throw new IllegalArgumentException("삭제된 게시글은 수정할 수 없습니다.");
        }

        // 변경된 사항이 있는지 확인
        boolean changed = post.isChanged(
                postRequestDto.getTitle(),
                postRequestDto.getContent(),
                postRequestDto.getImageUrl(),
                postRequestDto.getFileUrl(),
                postRequestDto.getCategory(),
                postRequestDto.getPostStatus()
        );

        if (!changed) {
            log.info("게시글 변경 사항 없음: postId={}", postId);
            return PostResponseDto.fromEntity(post);
        }

        // Redis 기존 캐시 삭제
        evictPostCache(postId);

        // kafka 이벤트 생성
        postEventProducer.sendPostUpdateEvent(postId);
        log.info("게시글 수정 완료: postId={}, userId={}", postId, userId);

        return PostResponseDto.fromEntity(post);
    }

    // 게시글 삭제 (Soft Delete)
    public void deletePost(Long postId, Long userId){

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 재처리 발생 가능성을 위한 멱등성(idempotent)
        // 멱등성이란 연산을 여러 번 적용하더라도 결과가 달라지지 않는 성질
        if (post.getPostStatus() == PostStatus.DELETED) {
            log.info("이미 삭제된 게시글 요청 무시: postId={}", postId);
            return;
        }

        // 비즈니스 메소드 - postStatus 를 NORMAL -> DELETED 변경
        post.delete();

        // Redis 캐시 삭제
        evictPostCache(postId);

        // Kafka 이벤트 생성
        postEventProducer.sendPostDeleteEvent(postId);
        log.info("게시글 삭제 완료: postId={}, userId={}", postId, userId);
    }

    // Redis 캐시 삭제 - 데이터가 수정되거나 삭제되면 캐시를 제거해서 데이터 불일치를 방지시킴.
    private void evictPostCache(Long postId){
        redisTemplate.delete(POST_DETAIL + postId);
    }
}
