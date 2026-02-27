package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.entity.PostLike;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostLikeService {

    // 게시글 좋아요
    public void likePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if(!userRepository.existsById(userId)){
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        PostLike postLike = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();

        postLikeRepository.save(postLike);

        post.setLikeCount(post.getLikeCount() + 1);
    }

    // 게시글 좋아요 취소
    public void unlikePost(Long postId, Long userId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new IllegalStateException("좋아요 기록이 없습니다."));

        postLikeRepository.delete(postLike);

        post.setLikeCount(post.getLikeCount() - 1);
    }
}
