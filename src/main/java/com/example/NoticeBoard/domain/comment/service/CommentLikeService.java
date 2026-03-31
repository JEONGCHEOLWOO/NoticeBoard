package com.example.NoticeBoard.domain.comment.service;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.entity.CommentLike;
import com.example.NoticeBoard.domain.user.entity.User;

public class CommentLikeService {

    // 댓글 좋아요
    public void likeComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        CommentLike commentLike = CommentLike.builder()
                .commentId(commentId)
                .userId(userId)
                .build();

        commentLikeRepository.save(commentLike);

        // 중복 좋아요 체크는 CommentLike 테이블 기준
        comment.setLikeCount(comment.getLikeCount() + 1);
    }

    // 댓글 좋아요 취소
    public void unlikeComment(Long commentId, Long userId) {

        CommentLike commentLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new IllegalStateException("좋아요 기록이 없습니다."));


        commentLikeRepository.delete(commentLike);

        Comment comment = commentLike.getCommentId();
        comment.setLikeCount(comment.getLikeCount() - 1);
    }
}
