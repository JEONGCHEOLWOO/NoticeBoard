package com.example.NoticeBoard.domain.comment.controller;

import com.example.NoticeBoard.global.security.CustomUserDetails;
import com.example.NoticeBoard.domain.comment.dto.CommentRequestDto;
import com.example.NoticeBoard.domain.comment.dto.CommentResponseDto;
import com.example.NoticeBoard.domain.comment.service.CommentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments/command")
@RequiredArgsConstructor
@Slf4j
public class CommentCommandController {

    private final CommentCommandService commentCommandService;

    // 댓글 생성
    // postId는 PathVariable로, userdetails는 AuthenticationPrincipal로, commentRequestDto는 RequestBody로 받은 이유는
    // 먼저 postId는 '이 댓글은 어떤 게시글에 속하는지'를 알기 위해서 URL에서 의도를 파악할 수 있도록 사용.
    @PostMapping("/{postId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentRequestDto commentRequestDto){
        log.info("댓글 작성 요청: postId={}, userId={}", postId, userDetails.getId());
        return ResponseEntity.ok(commentCommandService.createComment(postId, userDetails.getId(), commentRequestDto));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentRequestDto commentRequestDto) {
        log.info("댓글 수정 요청: commentId={}, userId={}", commentId, userDetails.getId());
        return ResponseEntity.ok(commentCommandService.updateComment(commentId, userDetails.getId(), commentRequestDto));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("댓글 삭제 요청: commentId={}, userId={}", commentId, userDetails.getId());
        commentCommandService.deleteComment(commentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
