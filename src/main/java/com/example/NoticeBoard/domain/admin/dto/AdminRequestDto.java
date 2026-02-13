package com.example.NoticeBoard.domain.admin.dto;

import com.example.NoticeBoard.global.enumeration.ReportReason;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminRequestDto {

    @NotBlank(message = "상세 사유를 입력해주세요.")
    private String detail; // 처리 사유
    
    private ReportReason reportReason; // 신고 사유
}
