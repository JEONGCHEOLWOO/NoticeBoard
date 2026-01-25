package com.example.NoticeBoard.controller;

import com.example.NoticeBoard.dto.AdminRequestDto;
import com.example.NoticeBoard.dto.AdminResponseDto;
import com.example.NoticeBoard.dto.AdminStatisticsDto;
import com.example.NoticeBoard.dto.ResponseDto;
import com.example.NoticeBoard.entity.AdminLog;
import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.service.AdminService;
import com.google.api.client.util.DateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // 게시글 블라인드 처리
    @PostMapping("/posts/{postId}/blind")
    public ResponseEntity<Void> blindPost(
            @PathVariable Long postId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.blindPostByAdmin(postId, admin, requestDto.getDetail(), requestDto.getReportReason());
        return ResponseEntity.noContent().build();
    }

    // 게시글 삭제 처리 (soft)
    @PostMapping("/posts/{postId}/delete")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.deletePostByAdmin(postId, admin, requestDto.getDetail(), requestDto.getReportReason());
        return ResponseEntity.noContent().build();
    }

    // 게시글 삭제 처리 (Hard)
    @DeleteMapping("/posts/{postId}/hard-delete")
    public ResponseEntity<Void> hardDeletePost(
            @PathVariable Long postId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.hardDeletePostByAdmin(postId, admin, requestDto.getDetail());
        return ResponseEntity.noContent().build();
    }

    // 댓글 블라인드 처리
    @PostMapping("/comments/{commentId}/blind")
    public ResponseEntity<Void> blindComment(
            @PathVariable Long commentId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.blindCommentByAdmin(commentId, admin, requestDto.getDetail(), requestDto.getReportReason());
        return ResponseEntity.noContent().build();
    }

    // 댓글 삭제 처리 (soft)
    @PostMapping("/comments/{commentId}/delete")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.deleteCommentByAdmin(commentId, admin, requestDto.getDetail(), requestDto.getReportReason());
        return ResponseEntity.noContent().build();
    }

    // 댓글 삭제 처리 (hard)
    @DeleteMapping("/comments/{commentId}/hard-delete")
    public ResponseEntity<Void> hardDeleteComment(
            @PathVariable Long commentId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.hardDeleteCommentByAdmin(commentId, admin, requestDto.getDetail());
        return ResponseEntity.noContent().build();
    }

    // 유저 벤 처리
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable Long userId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.banUser(userId, admin, requestDto.getDetail());
        return ResponseEntity.noContent().build();
    }

    // 유저 벤 취소
    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<Void> unbanUser(
            @PathVariable Long userId,
            @RequestBody AdminRequestDto requestDto,
            @AuthenticationPrincipal User admin) {
        adminService.unbanUser(userId, admin, requestDto.getDetail());
        return ResponseEntity.noContent().build();
    }

    // 관리자 통계 조회 (30일)
    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsDto> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate,
            @AuthenticationPrincipal User admin){
        if (startDate == null){
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        AdminStatisticsDto statisticsDto = adminService.getAdminStatistics(admin, startDate, endDate);
        return ResponseEntity.ok(statisticsDto);
    }

    // 대시보드 (7일)
    @GetMapping("/dashboard")
    public ResponseEntity<AdminStatisticsDto> getDashbord(@AuthenticationPrincipal User admin){
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        AdminStatisticsDto statisticsDto = adminService.getAdminStatistics(admin, startDate, endDate);
        return ResponseEntity.ok(statisticsDto);
    }

    // 최근 관리자 활동 로그 조회 (최근 50개)
    @GetMapping("/logs/recent")
    public ResponseEntity<List<AdminLog>> getRecentLogs(@AuthenticationPrincipal User admin){
        List<AdminLog> logs = adminService.getRecentAdminLogs(admin);
        return ResponseEntity.ok(logs);
    }

    // 기간별 관리자 활동 로그 조회
    @GetMapping("/logs")
    public ResponseEntity<List<AdminLog>> getLogsByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate,
            @AuthenticationPrincipal User admin){
        List<AdminLog> logs = adminService.getAdminLogsByDateRange(admin, startDate, endDate);
        return ResponseEntity.ok(logs);
    }

}
