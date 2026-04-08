package com.example.NoticeBoard.domain.comment.controller;

import com.example.NoticeBoard.domain.comment.service.CommentLikeService;
import com.example.NoticeBoard.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments/likes")
@RequiredArgsConstructor
@Slf4j
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    // 댓글 좋아요
    @PostMapping("/like/{commentId}")
    public ResponseEntity<Void> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentLikeService.likeComment(commentId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    // 댓글 좋아요 취소
    @PostMapping("/unlike/{commentId}")
    public ResponseEntity<Void> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentLikeService.unlikeComment(commentId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
}
