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
    public PostResponseDto createPost(Long userId, PostRequestDto requestDto){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        Post post = Post.builder()
                .category(requestDto.getCategory())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .imageUri(requestDto.getImageUri())
                .fileUri(requestDto.getFileUri())
                .userId(userId)
                .postStatus(PostStatus.NORMAL)
                .nickname(user.getNickname())
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .build();

        Post savedPost = postRepository.save(post);

        postEventProducer.sendPostCreatedEvent(savedPost.getId());
        log.info("게시글 생성 완료: postId={}, userId={}", savedPost.getId(), userId);

        return PostResponseDto.fromEntity(savedPost);
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
