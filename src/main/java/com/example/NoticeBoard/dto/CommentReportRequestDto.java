package com.example.NoticeBoard.dto;

import com.example.NoticeBoard.enumeration.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CommentReportRequestDto {
    @NotNull
    private ReportReason reason; // 신고 사유 (ENUM 권장)

    @Size(max = 500)
    private String text; // 상세 사유
}
