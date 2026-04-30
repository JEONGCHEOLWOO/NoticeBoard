package com.example.NoticeBoard.domain.comment.service;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.repository.CommentRepository;
import com.example.NoticeBoard.domain.report.dto.CommentReportRequestDto;
import com.example.NoticeBoard.domain.report.entity.CommentReport;
import com.example.NoticeBoard.domain.report.repository.CommentReportRepository;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import com.example.NoticeBoard.global.enumeration.CommentStatus;
import com.example.NoticeBoard.global.enumeration.ReportStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentReportService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;

    // 댓글 신고
    public void reportComment(Long commentId, Long userId, CommentReportRequestDto requestDto) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        if(!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        if (commentReportRepository.existsByCommentIdAndReporter(commentId, userId)) {
            throw new IllegalStateException("이미 신고한 댓글입니다.");
        }

        if(comment.getUserId().equals(userId)){
            throw new IllegalArgumentException("본인이 작성한 댓글은 신고할 수 없습니다.");
        }

        CommentReport report = CommentReport.builder()
                .commentId(commentId)
                .reporter(userId)
                .content(requestDto.getText())
                .reportReason(requestDto.getReason())
                .reportStatus(ReportStatus.PROCESSING)
                .build();

        commentReportRepository.save(report);

        long reportCount = commentReportRepository.countByCommentId(commentId);

        // 신고 5회 이상 → 자동 블라인드
        if (reportCount >= 5 && comment.getCommentStatus() != CommentStatus.BLIND) {
            comment.setCommentStatus(CommentStatus.BLIND);
        }
    }

}
