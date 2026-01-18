package com.example.NoticeBoard.dto;

import com.example.NoticeBoard.enumeration.ReportReason;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminRequestDto {

    private String detail; // 처리 사유
    
    private ReportReason reportReason; // 신고 사유
}
