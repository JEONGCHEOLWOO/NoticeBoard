package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.User;
import com.google.common.io.Files;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // create, delete, update, findId, findAll 필요.
    // 로그인 아이디 조회
    Optional<User> findByLoginId(String loginId);

    // 사용자 이름과 이메일로 조회
    Optional<User> findByLoginIdAndUsername(String loginId, String username);

    // 사용자 이름과 이메일로 조회
    Optional<User> findByUsernameAndEmail(String username, String email);

    // 사용자 이름과 전화번호로 조회
    Optional<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);

    // 아이디 중복 체크
    boolean existsByLoginId(String loginId);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    // 전화번호 중복 체크
    boolean existsByPhoneNumber(String phoneNumber);

    // 이메일로 유저 찾기 (소셜 로그인)
    Optional<User> findByEmail(String email);
}
