package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentReportRepository  extends JpaRepository<CommentReport, Long> {

    // 회원이 해당 댓글을 신고한 적 있는지
    boolean existsByCommentIdAndUserId(Long postId, Long userId);

}