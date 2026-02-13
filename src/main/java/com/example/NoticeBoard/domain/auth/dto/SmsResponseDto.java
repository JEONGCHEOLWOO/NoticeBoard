package com.example.NoticeBoard.domain.auth.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SmsResponseDto {

    private String messageId;

    private String code;

}
