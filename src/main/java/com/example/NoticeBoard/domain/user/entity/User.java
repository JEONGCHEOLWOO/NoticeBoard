package com.example.NoticeBoard.domain.user.entity;

import com.example.NoticeBoard.global.enumeration.AuthProvider;
import com.example.NoticeBoard.global.enumeration.Role;
import com.example.NoticeBoard.global.enumeration.Sex;
import com.example.NoticeBoard.global.enumeration.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 테이블 이름을 'user' 에서 'users' 로 바꾼 이유는 H2 부터는 'user'가 예약어에 포함되면서 변경하게 되었다.
// H2는 자바(Java)로 작성된 가볍고 빠른 오픈 소스 관계형 데이터베이스 관리 시스템(RDBMS)이다.
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 선언하는 어노테이션
    private Long id; // 내부 PK

    @Column(nullable = false, unique = true, length = 30)
    private String loginId; // 로그인 아이디

    @Column(nullable = false, unique = true, length = 50)
    private String nickname; // 닉네임

    @Column(nullable = false, length = 50)
    private String username; // 사용자 이름, 동명이인이 존재할 수 있으므로 unique 설정X

    @Column(nullable = false)
    private String password; // 비밀번호 -> 요즘엔 BCrypt 같은 해시 함수를 적용하는게 필수 -> Spring Security PasswordEncoder

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Sex sex; // Enum: MALE, FEMALE // 성별

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    @Column(nullable = false, unique = true, length = 20) // +820112345678 이 형태가 되야 될꺼 같음. 010-1234-5678 13자리 이상, 국제번호 (+82) +5~6자리
    private String phoneNumber; // 전화번호

    private String birthDate; // 생년월일 - 20250101

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role; // Enum: USER, ADMIN, SUPER_ADMIN // 역할

    private UserStatus userStatus; // 회원 상태 (일반 유저, 삭제된 유저)
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 회원가입 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 회원정보 수정 날짜
    
    private LocalDateTime deletedAt; // 회원 삭제 요청 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuthProvider provider; // Enum: Google, Kakao, Naver, FaceBook, Local // 소셜 로그인

    private String providerId; // 소셜 로그인 시 Id, Local로 로그인 시 null

}
