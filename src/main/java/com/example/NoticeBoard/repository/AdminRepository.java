package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.AdminLog;
import com.example.NoticeBoard.enumeration.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AdminRepository extends JpaRepository<AdminLog, Long> {

    List<AdminLog> findByReportTypeOrderByCreatedAtDesc(ReportType reportType);

    List<AdminLog> findByUser_Id(Long userId);

    Collection<Object> findDailyAdminStats();

    @Modifying
    @Query("""
        delete from User u
        where u.userStatus = 'DELETED'
        and u.deletedAt < :expiredAt
    """)
    int hardDeleteExpiredUsers(LocalDateTime expiredAt);
}
