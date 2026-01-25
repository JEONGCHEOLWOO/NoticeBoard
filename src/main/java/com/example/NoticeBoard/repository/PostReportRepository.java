package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.PostReport;
import com.example.NoticeBoard.enumeration.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostReportRepository  extends JpaRepository<PostReport, Long> {

    // 회원이 해당 게시글을 신고한 적 있는지
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // 특정 기간의 신고 조회
    List<PostReport> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 기간 신고 수 카운트
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 기간 + 상태별 신고 수 카운트
    Long countByReportStatusAndCreatedAtBetween(ReportStatus status, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 유저가 신고한 내역
    List<PostReport> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // 특정 유저의 신고 횟수
    Long countByUserIdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
