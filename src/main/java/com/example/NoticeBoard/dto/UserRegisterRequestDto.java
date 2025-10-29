package com.example.NoticeBoard.dto;

import com.example.NoticeBoard.enumeration.Sex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserRegisterRequestDto {

    @NotBlank
    private String loginId; // 로그인 아이디

    @NotBlank
    private String nickname; // 닉네임

    @NotBlank
    private String username; // 사용자 이름

    @NotBlank
    @Size(min = 8, message = "비밀번호는 영문, 숫자, 특수문자를 포함한 최소 8자리 이상이어야 합니다.")
    private String password; // 비밀번호

    @NotNull
    private Sex sex; // 성별

    @Email
    @NotBlank
    private String email; // 이메일

    @NotBlank
    private String phoneNumber; // 전화번호

    @NotBlank
    private String birthdate; // 생년월일
}
