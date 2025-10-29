package com.example.NoticeBoard.dto;

import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.enumeration.Role;
import com.example.NoticeBoard.enumeration.Sex;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id; // 내부 PK (식별자, 외부 UUID로 대체 가능)

    private String loginId; // 로그인 아이디

    private String username; // 사용자 이름

    private String nickname; // 닉네임

    private String email; // 이메일

    private Sex sex; // 성별

    private String birthdate; // 생년월일

    private LocalDateTime createdAt; // 가입일

    private Role role; // 역활 (USER, ADMIN, SUPER_ADMIN)

    public static UserResponseDto fromEntity(User user){
        return UserResponseDto.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .sex(user.getSex())
                .birthdate(user.getBirthDate())
                .createdAt(user.getCreatedAt())
                .role(user.getRole())
                .build();
    }
}
