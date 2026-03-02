package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.entity.PostLike;
import com.example.NoticeBoard.domain.post.repository.PostLikeRepository;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
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
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;

    // 게시글 좋아요
    public void likePost(Long postId, Long userId) {

        if(!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        }

        if(!userRepository.existsById(userId)){
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        PostLike postLike = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();

        postLikeRepository.save(postLike);

        postRepository.incrementLikeCount(postId);
    }

    // 게시글 좋아요 취소
    public void unlikePost(Long postId, Long userId) {

        if(!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("해당 게시글을 찾을 수 없습니다.");
        }

        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new IllegalStateException("좋아요 기록이 없습니다."));

        postLikeRepository.delete(postLike);

        postRepository.decrementLikeCount(postId);
    }
}
