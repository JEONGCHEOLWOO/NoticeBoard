package com.example.NoticeBoard.domain.comment.controller;

import com.example.NoticeBoard.domain.comment.service.CommentLikeService;
import com.example.NoticeBoard.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments/likes")
@RequiredArgsConstructor
@Slf4j
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    // 댓글 좋아요
    @PostMapping("/{commentId}")
    public ResponseEntity<Void> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("댓글 좋아요 요청: commentId={}, userId={}", commentId, userDetails.getId());
        commentLikeService.likeComment(commentId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    // 댓글 좋아요 취소
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("댓글 좋아요 요청 취소: commentId={}, userId={}", commentId, userDetails.getId());
        commentLikeService.unlikeComment(commentId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
