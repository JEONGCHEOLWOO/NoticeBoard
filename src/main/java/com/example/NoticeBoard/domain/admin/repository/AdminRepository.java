package com.example.NoticeBoard.domain.admin.repository;

import com.example.NoticeBoard.domain.admin.entity.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<AdminLog, Long> {

    // 특정 기간의 로그 조회
    List<AdminLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 최근 N개의 로그 조회
    List<AdminLog> findTop50ByOrderByCreatedAtDesc();

    // 특정 기간의 모든 로그
    List<AdminLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
}
