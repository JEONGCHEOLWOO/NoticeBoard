package com.example.NoticeBoard.domain.report.dto;

import com.example.NoticeBoard.global.enumeration.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PostReportRequestDto {
    @NotNull
    private ReportReason reason; // 신고 사유 (ENUM 권장)

    @Size(max = 500)
    private String content; // 상세 사유
}
