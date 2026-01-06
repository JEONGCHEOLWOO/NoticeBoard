package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostReportRepository  extends JpaRepository<PostReport, Long> {

    // 회원이 해당 게시글을 신고한 적 있는지
    boolean existsByPostIdAndUserId(Long postId, Long userId);

}
