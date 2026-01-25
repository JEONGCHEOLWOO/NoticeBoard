package com.example.NoticeBoard.controller;

import com.example.NoticeBoard.dto.*;
import com.example.NoticeBoard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입 - postman 확인 완료
    @PostMapping("/register")
    public ResponseEntity<ResponseDto<?>> register(@RequestBody @Valid UserRegisterRequestDto dto){
        UserResponseDto userResponseDto = userService.register(dto);
        return ResponseEntity.ok(ResponseDto.success(userResponseDto, "회원가입 성공"));
    }

    // 로그인 - postman 확인 완료
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<?>> login(@RequestBody @Valid LoginRequestDto dto){
        UserResponseDto userResponseDto = userService.login(dto);
        return ResponseEntity.ok(ResponseDto.success(userResponseDto, "로그인 성공"));
    }

    // 비밀번호 변경 - postman 확인 완료
    @PostMapping("/update/pw")
    public ResponseEntity<ResponseDto<?>> updatePw(@RequestBody @Valid FindPwRequestDto dto, @RequestParam String newPassword){
        String result = userService.updatePw(dto, newPassword);
        return ResponseEntity.ok(ResponseDto.success(result, "비밀번호 변경 성공"));
    }

    // 아이디 찾기 - postman 확인 완료
    @PostMapping("/find/id")
    public ResponseEntity<ResponseDto<?>> findId(@RequestBody @Valid FindIdRequestDto dto) {
        String result = userService.findId(dto);
        return ResponseEntity.ok(ResponseDto.success(result, "아이디 찾기 성공"));
    }

    // 인증번호 요청(이메일) - postman 확인 완료
    @PostMapping("/request/verificationCode/email")
    public ResponseEntity<ResponseDto<?>> emailVerificationCode(@RequestBody @Valid VerificationCodeRequestDto dto) {
        String result = userService.emailVerificationCode(dto);
        return ResponseEntity.ok(ResponseDto.success(result, "이메일 인증번호 전송 완료"));
    }

    // 인증번호 요청(전화번호) - postman 확인 완료
    @PostMapping("/request/verificationCode/phoneNumber")
    public ResponseEntity<ResponseDto<?>> phoneNumberVerificationCode(@RequestBody @Valid VerificationCodeRequestDto dto) {
        String result = userService.phoneNumberVerificationCode(dto);
        return ResponseEntity.ok(ResponseDto.success(result, "전화번호 인증번호 전송 완료"));
    }

}
