package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.domain.post.service.PostReportService;
import com.example.NoticeBoard.domain.report.dto.PostReportRequestDto;
import com.example.NoticeBoard.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/reports")
@RequiredArgsConstructor
@Slf4j
public class PostReportController {

    private final PostReportService postReportService;

    // 게시글 신고
    @PostMapping("/{postId}")
    public ResponseEntity<Void> reportPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostReportRequestDto postReportRequestDto){
        log.info("게시글 신고 요청: postId={}, userId={}", postId, userDetails.getId());
        postReportService.reportPost(postId, userDetails.getId(), postReportRequestDto);
        return ResponseEntity.ok().build();
    }
}
