package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.event.PostLikeProducer;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostLikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;
    private final PostLikeProducer postLikeProducer;

    public static final String POST_LIKE = "post:like:";

    // 게시글 좋아요
    public void likePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if(!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        if (post.getPostStatus() == PostStatus.DELETED){
            throw new IllegalArgumentException("삭제된 게시글에는 좋아요를 할 수 없습니다.");
        }

        String key = POST_LIKE + postId;
        // SADD post:like:postId userId -> SADD post:like:12 3 -> 게시글 12 좋아요 목록에 userId=3 추가
        // 여기서 Redis의 Set 명령어 SADD(Set Add) 실행. 1이면 새로 추가, 0이면 이미 좋아요가 되어있음.
        boolean isNew = redisTemplate.opsForSet().add(key, userId.toString()) == 1;

        if(!isNew){
            throw new IllegalArgumentException("이미 좋아요를 눌렀습니다.");
        }

        // Kafka 이벤트 발행
        postLikeProducer.sendLikeEvent(postId);
        log.info("게시글 좋아요 완료: postId={}, userId={}", postId, userId);
    }

    // 게시글 좋아요 취소
    public void unlikePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if(!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        if (post.getPostStatus() == PostStatus.DELETED){
            throw new IllegalArgumentException("삭제된 게시글에는 좋아요를 할 수 없습니다.");
        }

        String key = POST_LIKE + postId;
        // SREM post:like:postId userId -> SREM post:like:12 3 -> 게시글 12 좋아요 목록에서 userId=3 좋아요 취소
        // 여기서 Redis의 Set 명령어 SREM(Set Remove) 실행. 1이면 좋아요가 되어 있음, 0이면 좋아요 기록이 없음.
        boolean exists = redisTemplate.opsForSet().remove(key, userId.toString()) == 1;

        if(!exists) {
            throw new IllegalStateException("좋아요 기록이 없습니다.");
        }

        // Kafka 이벤트 발행
        postLikeProducer.sendUnlikeEvent(postId);
        log.info("게시글 좋아요 취소 완료: postId={}, userId={}", postId, userId);
    }
}
