package com.example.NoticeBoard.global.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 사용자 관련
    USER_NOT_FOUND("일치하는 회원이 없습니다."),
    INVALID_PASSWORD("비밀번호가 일치하지 않습니다."),

    // 유효성 관련
    DUPLICATE_LOGIN_ID("이미 존재하는 아이디입니다."),
    DUPLICATE_EMAIL("이미 존재하는 이메일입니다."),
    DUPLICATE_NICKNAME("이미 존재하는 닉네임입니다."),
    DUPLICATE_PHONE("이미 등록된 전화번호입니다."),
    PASSWORD_SAME_AS_OLD("기존 비밀번호와 동일합니다."),
    INVALID_PASSWORD_FORMAT("비밀번호 형식이 올바르지 않습니다."),
    UNSUPPORTED_EMAIL_DOMAIN("지원하지 않는 이메일 도메인입니다."),

    // 이메일 관련
    EMAIL_SEND_FAIL("이메일 전송에 실패했습니다."),

    // 서버 관련
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.");

    private final String message;
}

