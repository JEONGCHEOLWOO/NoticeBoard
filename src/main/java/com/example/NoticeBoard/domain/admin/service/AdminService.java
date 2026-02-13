package com.example.NoticeBoard.domain.admin.service;

import com.example.NoticeBoard.domain.admin.entity.AdminLog;
import com.example.NoticeBoard.domain.admin.repository.AdminRepository;
import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.repository.CommentRepository;
import com.example.NoticeBoard.domain.admin.dto.AdminStatisticsDto;
import com.example.NoticeBoard.domain.admin.dto.MultipleReportUserDto;
import com.example.NoticeBoard.domain.admin.dto.ReportStatistics;
import com.example.NoticeBoard.domain.admin.dto.UserMetrics;
import com.example.NoticeBoard.enumeration.*;
import com.example.NoticeBoard.global.enumeration.*;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.report.entity.CommentReport;
import com.example.NoticeBoard.domain.report.entity.PostReport;
import com.example.NoticeBoard.domain.report.repository.CommentReportRepository;
import com.example.NoticeBoard.domain.report.repository.PostReportRepository;
import com.example.NoticeBoard.domain.user.entity.User;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostReportRepository postReportRepository;
    private final CommentReportRepository commentReportRepository;
    private final AdminRepository adminRepository;

    // ADMIN 검증 - 프론트에서도 ADMIN을 검증하는데 여기에서도 ADMIN인지 검증하는 이유: API를 직접적으로 호출하거나 JS조작을 통해서 API를 호출하는 경우, POSTMAN이나 CURL, 모바일 앱에서 호출하는 경우 보안에 취약해질 수 있음.
    private void validateAdmin(User admin){
        if(admin == null || admin.getRole() != Role.ADMIN){
            throw new AccessDeniedException("관리자 권한이 없습니다.");
        }
    }

    // 게시글 블라인드 처리
    public void blindPostByAdmin(Long postId, User admin, String detail, ReportReason reportReason) {
        validateAdmin(admin);
        PostReport postReport = postReportRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("신고된 게시글을 찾을 수 없습니다."));

        Post post = postReport.getPost();
        post.setPostStatus(PostStatus.BLIND);

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.BLIND_POST)
                .user(postReport.getUser())
                .post(post)
                .reportType(ReportType.POST)
                .reportReason(reportReason)
                .detail(detail)
                .build();

        postReport.setReportStatus(ReportStatus.RESOLVED);
        postReport.setResolvedAt(LocalDateTime.now());
        adminRepository.save(log);
    }

    // 게시글 삭제 처리 (Soft Delete)
    public void deletePostByAdmin(Long postId, User admin, String detail, ReportReason reportReason){
        validateAdmin(admin);
        PostReport postReport = postReportRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("신고된 게시글을 찾을 수 없습니다."));

        Post post = postReport.getPost();
        post.setPostStatus(PostStatus.DELETED);
        
        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.DELETE_POST)
                .user(post.getUser())
                .post(post)
                .reportType(ReportType.POST)
                .reportReason(reportReason)
                .detail(detail)
                .build();

        postReport.setReportStatus(ReportStatus.RESOLVED);
        postReport.setResolvedAt(LocalDateTime.now());
        adminRepository.save(log);

    }

    // 게시글 영구 삭제 (Hard Delete)
    public void hardDeletePostByAdmin(Long postId, User admin, String detail) {
        validateAdmin(admin);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("신고된 게시글을 찾을 수 없습니다."));

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.DELETE_POST) // 하드 삭제도 DELETE로 기록
                .user(post.getUser())
                .post(null) // 삭제 후에는 참조 불가
                .reportType(ReportType.POST)
                .detail("HARD DELETE: " + detail)
                .build();

        adminRepository.save(log);
        // 관련 댓글, 좋아요, 신고 등 모두 삭제됨, 영구 삭제 (CASCADE 설정에 따라)
        postRepository.delete(post);
    }

    // 댓글 블라인드 처리
    public void blindCommentByAdmin(Long commentId, User admin, String detail, ReportReason reportReason){
        validateAdmin(admin);
        CommentReport commentReport = commentReportRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("신고된 댓글을 찾을 수 없습니다."));

        Comment comment = commentReport.getComment();
        comment.setCommentStatus(CommentStatus.BLIND);

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.BLIND_COMMENT)
                .user(comment.getUser())
                .comment(comment)
                .reportType(ReportType.COMMENT)
                .reportReason(reportReason)
                .detail(detail)
                .build();

        commentReport.setReportStatus(ReportStatus.RESOLVED);
        commentReport.setResolvedAt(LocalDateTime.now());
        adminRepository.save(log);

    }
    
    // 댓글 삭제 처리 (Soft Delete)
    public void deleteCommentByAdmin(Long commentId, User admin, String detail, ReportReason reportReason) {
        validateAdmin(admin);
        CommentReport commentReport = commentReportRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("신고된 댓글을 찾을 수 없습니다."));

        Comment comment = commentReport.getComment();
        comment.setCommentStatus(CommentStatus.DELETED);

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.DELETE_COMMENT)
                .user(comment.getUser())
                .comment(comment)
                .reportType(ReportType.COMMENT)
                .reportReason(reportReason)
                .detail(detail)
                .build();

        commentReport.setReportStatus(ReportStatus.RESOLVED);
        commentReport.setResolvedAt(LocalDateTime.now());
        adminRepository.save(log);
    }

    // 댓글 영구 삭제 (Hard Delete)
    public void hardDeleteCommentByAdmin(Long commentId, User admin, String detail) {
        validateAdmin(admin);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("신고된 댓글을 찾을 수 없습니다."));

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.DELETE_COMMENT) // 하드 삭제도 DELETE로 기록
                .user(comment.getUser())
                .comment(null) // 삭제 후에는 참조 불가
                .reportType(ReportType.COMMENT)
                .detail("HARD DELETE: " + detail)
                .build();

        adminRepository.save(log);
        // 관련 댓글, 좋아요, 신고 등 모두 삭제됨, 영구 삭제 (CASCADE 설정에 따라)
        commentRepository.delete(comment);
    }

    // 유저 차단 처리 - 유저를 신고하는 이유? -> 게시글이나 댓글에 신고를 많이 당한 경우 - 유저의 신고당한 횟수를 확인해서 처리
    public void banUser(Long userId, User admin, String detail){
        validateAdmin(admin);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setUserStatus(UserStatus.BANNED);

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.BAN_USER)
                .user(user)
                .detail(detail)
                .build();

        adminRepository.save(log);
    }

    // 유저 차단 해제
    public void unbanUser(Long userId, User admin, String detail){
        validateAdmin(admin);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setUserStatus(UserStatus.NORMAL);

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.UNBAN_USER)
                .user(user)
                .detail(detail)
                .build();

        adminRepository.save(log);
    }

    // 관리자 통계
    @Transactional
    public AdminStatisticsDto getAdminStatistics(User admin, LocalDate startDate,LocalDate endDate) {
        validateAdmin(admin);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23,59,59);

        return AdminStatisticsDto.builder()
                .reportStats(getReportStatistics(startDateTime, endDateTime))
                .userMetrics(getUserMetrics(startDateTime, endDateTime))
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    // 신고 관련 통계
    private ReportStatistics getReportStatistics(LocalDateTime startDate, LocalDateTime endDate){
        // 게시글 신고 목록 조회
        List<PostReport> postReports = postReportRepository.findByCreatedAtBetween(startDate, endDate);

        // 댓글 신고 목록 조회
        List<CommentReport> commentReports = commentReportRepository.findByCreatedAtBetween(startDate, endDate);

        // 일별 신고 수 계산 (게시글)
        Map<LocalDate, Long> postDailyReports = postReports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getCreatedAt().toLocalDate(),
                        Collectors.counting()));

        // 일별 신고 수 계산 (댓글)
        Map<LocalDate, Long> commentDailyReports = commentReports.stream()
                .collect(Collectors.groupingBy(
                        report -> report.getCreatedAt().toLocalDate(),
                        Collectors.counting()));

        // 일별 신고 수 합산
        Map<LocalDate, Long> dailyReports = new HashMap<>(postDailyReports);
        commentDailyReports.forEach((date, count) -> dailyReports.merge(date, count, Long::sum));

        // -----------------------------------------------------------------------------------------------

        // 신고 사유별 신고 수 (게시글)
        Map<ReportReason, Long> postReasonDistribution = postReports.stream()
                .collect(Collectors.groupingBy(
                        PostReport::getReportReason,
                        Collectors.counting()
                ));

        // 신고 사유별 신고 수 (댓글)
        Map<ReportReason, Long> commentReasonDistribution = commentReports.stream()
                .collect(Collectors.groupingBy(
                        CommentReport::getReportReason,
                        Collectors.counting()
                ));

        // 신고 사유별 신고 수 합산
        Map<ReportReason, Long> reasonDistribution = new HashMap<>(postReasonDistribution);
        commentReasonDistribution.forEach((reason, count) -> reasonDistribution.merge(reason, count, Long::sum));

        // -----------------------------------------------------------------------------------------------

        // 다중 신고 유저 (5회 이상)
        List<MultipleReportUserDto> multipleReporters = getMultipleReporters(postReports, commentReports, 5);

        // 상태별 신고 수 (처리중, 처리완료)
        long pendingPostReports = postReports.stream()
                .filter(r -> r.getReportStatus() == ReportStatus.PROCESSING)
                .count();

        long pendingCommentReports = commentReports.stream()
                .filter(r -> r.getReportStatus() == ReportStatus.PROCESSING)
                .count();

        long resolvedPostReports = postReports.stream()
                .filter(r -> r.getReportStatus() == ReportStatus.RESOLVED)
                .count();

        long resolvedCommentReports = commentReports.stream()
                .filter(r -> r.getReportStatus() == ReportStatus.RESOLVED)
                .count();

        return ReportStatistics.builder()
                .dailyReportCount(dailyReports)
                .reportReasonDistribution(reasonDistribution)
                .postReportCount((long) postReports.size())
                .commentReportCount((long) commentReports.size())
                .multipleReporters(multipleReporters)
                .pendingReportCount(pendingPostReports + pendingCommentReports)
                .resolvedReportCount(resolvedPostReports + resolvedCommentReports)
                .build();
    }

    // 다중 신고
    private List<MultipleReportUserDto> getMultipleReporters(
            List<PostReport> postReports,
            List<CommentReport> commentReports,
            int threshold
    ){
        // 게시글 신고자 Id
        Map<Long, List<PostReport>> postReportsByUser = postReports.stream()
                .collect(Collectors.groupingBy(r -> r.getUser().getId()));

        // 댓글 신고자 Id
        Map<Long, List<CommentReport>> commentReportsByUser = commentReports.stream()
                .collect(Collectors.groupingBy(r -> r.getUser().getId()));

        // 모든 신고자 ID 수집
        Set<Long> allReporterIds = new HashSet<>();
        allReporterIds.addAll(postReportsByUser.keySet());
        allReporterIds.addAll(commentReportsByUser.keySet());

        List<MultipleReportUserDto> result = new ArrayList<>();

        for (Long userId : allReporterIds) {
            List<PostReport> userPostReports = postReportsByUser.getOrDefault(userId, new ArrayList<>());
            List<CommentReport> userCommentReports = commentReportsByUser.getOrDefault(userId, new ArrayList<>());

            long totalCount = userPostReports.size() + userCommentReports.size();

            if (totalCount >= threshold) {
                // 신고 사유별 집계
                Map<ReportReason, Long> reasonCount = new HashMap<>();

                // 게시글 신고 사유별 집계
                userPostReports.stream()
                        .collect(Collectors.groupingBy(PostReport::getReportReason, Collectors.counting()))
                        .forEach((reason, count) -> reasonCount.merge(reason, count, Long::sum));

                // 댓글 신고 사유별 집계
                userCommentReports.stream()
                        .collect(Collectors.groupingBy(CommentReport::getReportReason, Collectors.counting()))
                        .forEach((reason, count) -> reasonCount.merge(reason, count, Long::sum));

                // 최근 신고 시간
                LocalDateTime lastReportTime = Stream.concat(
                        userPostReports.stream().map(PostReport::getCreatedAt),
                        userCommentReports.stream().map(CommentReport::getCreatedAt)
                ).max(LocalDateTime::compareTo).orElse(null);

                // 유저 정보
                User reporter = !userPostReports.isEmpty()
                        ? userPostReports.get(0).getUser()
                        : userCommentReports.get(0).getUser();

                result.add(MultipleReportUserDto.builder()
                        .reporterId(userId)
                        .reporterName(reporter.getEmail())
                        .reportCount(totalCount)
                        .lastReportTime(lastReportTime)
                        .reportReasonCount(reasonCount)
                        .build());
            }
        }

        return result.stream()
                .sorted(Comparator.comparing(MultipleReportUserDto::getReportCount).reversed())
                .collect(Collectors.toList());
    }

    // 사용자 가입 탈퇴
    private UserMetrics getUserMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        // 신규 가입자 수
        Long newUsers = userRepository.countByCreatedAtBetween(startDate, endDate);

        // 탈퇴자 수
        Long withdrawals = userRepository.countByUserStatusAndUpdatedAtBetween(UserStatus.DELETE, startDate, endDate);

        return UserMetrics.builder()
                .newUserCount(newUsers)
                .withdrawalCount(withdrawals)
                .build();
    }

    // 최근 관리자 로그    
    @Transactional
    public List<AdminLog> getRecentAdminLogs(User admin) {
        validateAdmin(admin);
        return adminRepository.findTop50ByOrderByCreatedAtDesc();
    }

    // 특정 기간 관리자 로그 조회
    @Transactional
    public List<AdminLog> getAdminLogsByDateRange(User admin, LocalDate startDate, LocalDate endDate) {
        validateAdmin(admin);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return adminRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDateTime, endDateTime);
    }
}
