package com.example.NoticeBoard.domain.admin.dto;

import com.example.NoticeBoard.global.enumeration.ReportReason;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
// 신고 관련 통계
public class ReportStatistics {

    private Map<LocalDate, Long> dailyReportCount;  // 일별 신고 수 (날짜 -> 신고 건수)

    private Map<ReportReason, Long> reportReasonDistribution;   // 신고 유형별 분포 (신고 사유 -> 건수)

    private Long postReportCount;   // 게시글 신고 수

    private Long commentReportCount;    // 댓글 신고 수

    private List<MultipleReportUserDto> multipleReporters;  // 다중 신고 유저 목록

    private Long pendingReportCount;    // 미처리 신고 수

    private Long resolvedReportCount;   // 처리 완료 신고 수
}