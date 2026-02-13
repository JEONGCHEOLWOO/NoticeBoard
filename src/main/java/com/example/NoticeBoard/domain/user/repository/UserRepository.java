package com.example.NoticeBoard.domain.user.repository;

import com.example.NoticeBoard.domain.user.entity.User;
import com.example.NoticeBoard.global.enumeration.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // JpaRepository에서 CRUDRepository(Create, Read, Update, Delete)를 상속받고 있어
    // create, update, delte, findAll, save 등의 메소드를 사용할 수 있음.
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

    // 특정 기간에 생성된 유저 수
    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 기간에 특정 상태가 된 유저 수
    Long countByUserStatusAndUpdatedAtBetween(UserStatus status, LocalDateTime startDate, LocalDateTime endDate);

}
