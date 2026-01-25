package com.example.NoticeBoard.dto;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminStatisticsDto {

    private ReportStatistics reportStats; // 신고 관련 통계

    private UserMetrics userMetrics; // 사용자 가입/탈퇴 지표

    private LocalDate startDate;
    private LocalDate endDate;

}
