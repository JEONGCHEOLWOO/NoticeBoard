package com.example.NoticeBoard.service;

import com.example.NoticeBoard.entity.*;
import com.example.NoticeBoard.enumeration.*;
import com.example.NoticeBoard.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;
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
                .orElseThrow();

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
        adminRepository.save(log);
    }

    // 게시글 삭제 처리
    public void deletePostByAdmin(Long postId, User admin, String detail, ReportReason reportReason){

        validateAdmin(admin);

        PostReport postReport = postReportRepository.findById(postId)
                .orElseThrow();

        Post post = postReport.getPost();
        post.setPostStatus(PostStatus.DELETED);

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.DELETE_POST)
                .user(postReport.getUser())
                .post(post)
                .reportType(ReportType.POST)
                .reportReason(reportReason)
                .detail(detail)
                .build();

        postReport.setReportStatus(ReportStatus.RESOLVED);
        adminRepository.save(log);

    }

    // 댓글 블라인드 처리
    public void blindCommentByAdmin(Long commentId, User admin, String detail, ReportReason reportReason){

        validateAdmin(admin);

        CommentReport commentReport = commentReportRepository.findById(commentId)
                .orElseThrow();

        Comment comment = commentReport.getComment();
        comment.setCommentStatus(CommentStatus.DELETED);

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
        adminRepository.save(log);

    }
    
    // 댓글 삭제 처리
    public void deleteCommentByAdmin(Long commentId, User admin, String detail, ReportReason reportReason) {

        validateAdmin(admin);

        CommentReport commentReport = commentReportRepository.findById(commentId)
                .orElseThrow();

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
        adminRepository.save(log);
    }

    // 유저 벤 처리 - 유저를 신고하는 이유? -> 게시글이나 댓글에 신고를 많이 당한 경우 - 유저의 신고당한 횟수를 확인해서 처리
    public void banUser(Long userId, User admin, String detail){

        validateAdmin(admin);

        User user = userRepository.findById(userId).orElseThrow();

        AdminLog log = AdminLog.builder()
                .admin(admin)
                .actionType(ActionType.BAN_USER)
                .user(user)
                .detail(detail)
                .build();

        adminRepository.save(log);
    }

    // 유저 벤 처리 취소
    public void unbanUser(Long userId, User admin, String detail){

        validateAdmin(admin);

        User user = userRepository.findById(userId).orElseThrow();

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
    public Map<LocalDate, Map<ActionType, Long>> getDailyStats(User admin) {
        validateAdmin(admin);

        return adminRepository.findDailyAdminStats()
                .stream()
                .collect(Collectors.groupingBy(
                        row -> (LocalDate) row[0],
                        Collectors.groupingBy(
                                row -> (ActionType) row[1],
                                Collectors.counting()
                        )
                ));
    }

    @Transactional
    public List<AdminLog> getLogs(User admin) {

        validateAdmin(admin);
        return adminRepository.findAll();
    }
}
