package com.example.NoticeBoard.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FindIdRequestDto {

    @NotBlank
    private String username; // 사용자 이름

    @Email
    private String email; // 이메일
    
    private String phoneNumber; // 전화번호
}
