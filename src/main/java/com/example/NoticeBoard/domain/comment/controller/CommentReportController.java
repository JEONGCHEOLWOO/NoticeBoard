package com.example.NoticeBoard.domain.comment.controller;

import com.example.NoticeBoard.domain.comment.service.CommentReportService;
import com.example.NoticeBoard.domain.report.dto.CommentReportRequestDto;
import com.example.NoticeBoard.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments/report")
@RequiredArgsConstructor
@Slf4j
public class CommentReportController {

    private final CommentReportService commentReportService;

    // 댓글 신고
    @PostMapping("/report/{commentId}")
    public ResponseEntity<Void> reportComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CommentReportRequestDto commentReportRequestDto) {
        commentReportService.reportComment(commentId, userDetails.getId(), commentReportRequestDto);
        return ResponseEntity.ok().build();
    }
}
