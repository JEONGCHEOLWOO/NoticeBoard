package com.example.NoticeBoard.domain.admin.dto;

import com.example.NoticeBoard.global.enumeration.ActionType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
// 관리자 활동 로그 DTO
public class AdminActivityDto {

    private Long adminId;   // 관리자 ID

    private String adminName;   // 관리자 이름/이메일

    private Long totalActionCount;  // 총 처리 건수

    private Map<ActionType, Long> actionTypeCount;  // 조치 유형별 건수 (ActionType -> 건수)

    private LocalDateTime lastActionTime;   // 최근 활동 시간

    private Double averageProcessingTimeMinutes;    // 평균 처리 시간 (분)
}
