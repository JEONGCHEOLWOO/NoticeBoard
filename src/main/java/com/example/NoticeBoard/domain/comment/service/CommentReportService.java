package com.example.NoticeBoard.domain.comment.service;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.report.dto.CommentReportRequestDto;
import com.example.NoticeBoard.domain.report.entity.CommentReport;
import com.example.NoticeBoard.domain.user.entity.User;
import com.example.NoticeBoard.global.enumeration.CommentStatus;
import com.example.NoticeBoard.global.enumeration.ReportStatus;

public class CommentReportService {


    // 댓글 신고
    public void reportComment(Long commentId, Long userId, CommentReportRequestDto requestDto) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        if (commentReportRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new IllegalStateException("이미 신고한 댓글입니다.");
        }

        if(comment.getUser().getId().equals(userId)){
            throw new IllegalArgumentException("본인이 작성한 댓글은 신고할 수 없습니다.");
        }

        CommentReport report = CommentReport.builder()
                .comment(comment)
                .user(user)
                .reportReason(requestDto.getReason())
                .content(requestDto.getText())
                .reportStatus(ReportStatus.PROCESSING)
                .build();

        commentReportRepository.save(report);

        // 신고 5회 이상 → 자동 블라인드
        if (comment.getReports().size() >= 5) {
            comment.setCommentStatus(CommentStatus.BLIND);
        }
    }

}
