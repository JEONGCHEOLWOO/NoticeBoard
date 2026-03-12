package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.event.PostLikeProducer;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final StringRedisTemplate redisTemplate;
    private final PostLikeProducer postLikeProducer;

    // 게시글 좋아요
    public void likePost(Long postId, Long userId) {

        if(!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        }

        if(!userRepository.existsById(userId)){
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        String key = "post:like:" + postId;
        boolean isNew = redisTemplate.opsForSet().add(key, userId.toString()) == 1;

        if(!isNew){
            throw new IllegalArgumentException("이미 좋아요를 눌렀습니다.");
        }

        postLikeProducer.sendLikeEvent(postId);
    }

    // 게시글 좋아요 취소
    public void unlikePost(Long postId, Long userId) {

        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        }

        String key = "post:like:" + postId;

        boolean exists = redisTemplate.opsForSet().remove(key, userId.toString()) == 1;

        if(!exists) {
            throw new IllegalStateException("좋아요 기록이 없습니다.");
        }

        postLikeProducer.sendUnlikeEvent(postId);
    }
}
