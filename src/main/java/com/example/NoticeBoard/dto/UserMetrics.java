package com.example.NoticeBoard.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
// 사용자 지표
public class UserMetrics {

    private Long newUserCount;  // 신규 가입자 수

    private Long withdrawalCount;   // 탈퇴자 수
}