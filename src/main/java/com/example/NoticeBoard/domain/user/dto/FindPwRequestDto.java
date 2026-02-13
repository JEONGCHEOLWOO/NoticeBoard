package com.example.NoticeBoard.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FindPwRequestDto {

    @NotBlank
    private String username; // 사용자 이름

    @NotBlank
    private String loginId; // 로그인 아이디

}
