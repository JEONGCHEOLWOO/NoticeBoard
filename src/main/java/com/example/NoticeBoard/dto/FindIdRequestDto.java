package com.example.NoticeBoard.dto;

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
    @NotBlank
    private String email; // 이메일
    
    @NotBlank
    private String phoneNumber; // 전화번호
}
