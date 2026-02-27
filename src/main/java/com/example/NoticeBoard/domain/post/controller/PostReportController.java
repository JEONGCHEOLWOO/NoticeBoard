package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.domain.post.service.PostReportService;
import com.example.NoticeBoard.domain.report.dto.PostReportRequestDto;
import com.example.NoticeBoard.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/report")
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
        postReportService.reportPost(postId, userDetails.getId(), postReportRequestDto);
        return ResponseEntity.ok().build();
    }
}
