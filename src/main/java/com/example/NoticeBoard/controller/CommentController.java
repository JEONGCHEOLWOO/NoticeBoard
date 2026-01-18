package com.example.NoticeBoard.controller;

import com.example.NoticeBoard.CustomUserDetails;
import com.example.NoticeBoard.dto.CommentReportRequestDto;
import com.example.NoticeBoard.dto.CommentRequestDto;
import com.example.NoticeBoard.dto.CommentResponseDto;
import com.example.NoticeBoard.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    // postId는 PathVariable로, userdetails는 AuthenticationPrincipal로, commentRequestDto는 RequestBody로 받은 이유는
    // 먼저 postId는 '이 댓글은 어떤 게시글에 속하는지'를 알기 위해서 URL에서 의도를 파악할 수 있도록 사용.
    @PostMapping("/create/{postId}")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentRequestDto commentRequestDto){
        return ResponseEntity.ok(commentService.createComment(postId, userDetails.getId(), commentRequestDto));
    }

    // 댓글 수정
    @PostMapping("/update/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentRequestDto commentRequestDto) {
        return ResponseEntity.ok(commentService.updateComment(commentId, userDetails.getId(), commentRequestDto));
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // 댓글 좋아요
    @PostMapping("/like/{commentId}")
    public ResponseEntity<Void> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.likeComment(commentId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    // 댓글 좋아요 취소
    @PostMapping("/unlike/{commentId}")
    public ResponseEntity<Void> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        commentService.unlikeComment(commentId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    // 댓글 신고
    @PostMapping("/report/{commentId}")
    public ResponseEntity<Void> reportComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentReportRequestDto commentReportRequestDto) {
        commentService.reportComment(commentId, userDetails.getId(), commentReportRequestDto);
        return ResponseEntity.ok().build();
    }
}
