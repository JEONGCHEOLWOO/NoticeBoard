package com.example.NoticeBoard.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 이메일, SMS 인증번호 요청 DTO
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class VerificationCodeRequestDto {

    @NotBlank
    private String username; // 사용자 이름

    @Email
    private String email; // 이메일

    private String phoneNumber; // 전화번호

    @NotBlank
    private String loginId; // 로그인 아이디

}
