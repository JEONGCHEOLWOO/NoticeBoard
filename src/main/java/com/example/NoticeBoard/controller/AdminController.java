package com.example.NoticeBoard.controller;

import com.example.NoticeBoard.dto.AdminRequestDto;
import com.example.NoticeBoard.entity.AdminLog;
import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 게시글 블라인드 처리
    @PostMapping("/reports/posts/blind/{postId}")
    public void blindPostByAdmin(@PathVariable Long postId,
                                 @RequestBody AdminRequestDto requestDto,
                                 @AuthenticationPrincipal User admin
    ) {
        adminService.blindPostByAdmin(postId, admin, requestDto.getDetail(), requestDto.getReportReason());
    }

    // 게시글 삭제 처리
    @PostMapping("/reports/posts/delete/{postId}")
    public void deletePostByAdmin(@PathVariable Long postId,
                                 @RequestBody AdminRequestDto requestDto,
                                 @AuthenticationPrincipal User admin
    ) {
        adminService.deletePostByAdmin(postId, admin, requestDto.getDetail(), requestDto.getReportReason());
    }

    // 댓글 블라인드 처리
    @PostMapping("/reports/comments/blind/{commentId}")
    public void blindCommentByAdmin(@PathVariable Long commentId,
                                 @RequestBody AdminRequestDto requestDto,
                                 @AuthenticationPrincipal User admin
    ) {
        adminService.blindCommentByAdmin(commentId, admin, requestDto.getDetail(), requestDto.getReportReason());
    }

    // 댓글 삭제 처리
    @PostMapping("/reports/comments/delete/{commentId}")
    public void deleteCommentByAdmin(@PathVariable Long commentId,
                                  @RequestBody AdminRequestDto requestDto,
                                  @AuthenticationPrincipal User admin
    ) {
        adminService.deleteCommentByAdmin(commentId, admin, requestDto.getDetail(), requestDto.getReportReason());
    }

    // 유저 벤 처리
    @PostMapping("/users/{userId}/ban")
    public void banUser(
            @PathVariable Long userId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin
    ) {
        adminService.banUser(userId, admin, requestDto.getDetail());
    }

    // 유저 벤 취소
    @PostMapping("/users/{userId}/unban")
    public void unbanUser(
            @PathVariable Long userId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin
    ) {
        adminService.unbanUser(userId, admin, requestDto.getDetail());
    }

    // 관리자 로그
    @GetMapping("/logs")
    public List<AdminLog> logs(@AuthenticationPrincipal User admin) {
        return adminService.getLogs(admin);
    }

    // 통계
    @GetMapping("/stats/daily")
    public Map<LocalDate, Map<?, Long>> dailyStats(
            @AuthenticationPrincipal User admin
    ) {
        return adminService.getDailyStats(admin);
    }
}
