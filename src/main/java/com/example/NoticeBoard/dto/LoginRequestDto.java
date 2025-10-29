package com.example.NoticeBoard.dto;

import com.example.NoticeBoard.enumeration.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginRequestDto {

    @NotBlank
    private String loginId; // 로그인 아이디

    @NotBlank
    private String password; // 비밀번호

    private AuthProvider provider; // 로그인 종류 (Local, Google, Naver, Kakao, Facebook 등)

    private String providerId; // 소셜 로그인 아이디

}
