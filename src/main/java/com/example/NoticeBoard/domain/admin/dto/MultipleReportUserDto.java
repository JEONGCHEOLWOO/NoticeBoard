package com.example.NoticeBoard.domain.admin.dto;

import com.example.NoticeBoard.global.enumeration.ReportReason;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
// 다중 신고 유저 DTO
public class MultipleReportUserDto {

    private Long reporterId;    // 신고자 ID

    private String reporterName;    // 신고자 이름/이메일

    private Long reportCount;   // 신고 횟수

    private LocalDateTime lastReportTime;   // 최근 신고 시간

    private Map<ReportReason, Long> reportReasonCount;  // 신고 유형 분포
}
