package com.example.NoticeBoard.entity;

import com.example.NoticeBoard.enumeration.AuthProvider;
import com.example.NoticeBoard.enumeration.Role;
import com.example.NoticeBoard.enumeration.Sex;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "user")
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

    private String birthDate; // 생년월일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role; // Enum: USER, ADMIN, SUPER_ADMIN // 역할

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 회원가입 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 회원정보 수정 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuthProvider provider; // Enum: Google, Kakao, Naver, FaceBook, Local // 소셜 로그인

    private String providerId; // 소셜 로그인 시 Id, Local로 로그인 시 null

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>(); // 게시글 목록

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>(); // 댓글 목록
}
