package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.domain.post.service.PostLikeService;
import com.example.NoticeBoard.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/likes")
@RequiredArgsConstructor
@Slf4j
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 게시글 좋아요
    @PostMapping("/{postId}")
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("게시글 좋아요 요청: postId={}, userId={}", postId, userDetails.getId());
        postLikeService.likePost(postId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    // 게시글 좋아요 취소
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("게시글 좋아요 요청 취소: postId={}, userId={}", postId, userDetails.getId());
        postLikeService.unlikePost(postId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

}
