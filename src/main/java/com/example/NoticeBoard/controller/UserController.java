package com.example.NoticeBoard.controller;

import com.example.NoticeBoard.dto.*;
import com.example.NoticeBoard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Valid UserRegisterRequestDto dto){
        return userService.register(dto);
    }

    // 로그인
    @PostMapping("/login")
    public UserResponseDto login(@RequestBody @Valid LoginRequestDto dto){
        return userService.login(dto);
    }

    // 비밀번호 변경
    @PostMapping("/update/pw")
    public String updatePw(@RequestBody @Valid FindPwRequestDto dto, @RequestParam String newPassword){
        return userService.updatePw(dto, newPassword);
    }

    // 아이디 찾기
    @PostMapping("/find/id")
    public String findId(@RequestBody @Valid FindIdRequestDto dto) {
        return userService.findId(dto);
    }

    // 인증번호 요청(이메일)
    @PostMapping("/request/verificationCode/email")
    public String emailVerificationCode(@RequestBody @Valid VerificationCodeRequestDto dto) {
        return userService.emailVerificationCode(dto);
    }

    // 인증번호 요청(전화번호)
    @PostMapping("/request/verificationCode/phoneNumber")
    public String phoneNumberVerificationCode(@RequestBody @Valid VerificationCodeRequestDto dto) {
        return userService.phoneNumberVerificationCode(dto);
    }

}
