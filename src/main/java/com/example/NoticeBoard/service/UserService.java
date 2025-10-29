package com.example.NoticeBoard.service;

import com.example.NoticeBoard.dto.*;
import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.enumeration.AuthProvider;
import com.example.NoticeBoard.enumeration.Role;
import com.example.NoticeBoard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final GmailService gmailService;
    private final NaverMailService naverMailService;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화(BCrypt)
    private final AwsSmsService awsSmsService;

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();


    // 회원가입
    public UserResponseDto register(UserRegisterRequestDto registerRequestDto){
        if(userRepository.existsByLoginId(registerRequestDto.getLoginId())){
            throw new IllegalArgumentException("이미 존재하는 아이디 입니다.");
        }
        if(userRepository.existsByEmail(registerRequestDto.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        if(userRepository.existsByNickname(registerRequestDto.getNickname())){
            throw new IllegalArgumentException("중복된 닉네임이 존재합니다.");
        }
        if(userRepository.existsByPhoneNumber(registerRequestDto.getPhoneNumber())){
            throw new IllegalArgumentException("이미 해당 전화번호로 가입된 아이디가 있습니다.");
        }

        // 🔒 비밀번호 유효성 검사
        validatePassword(registerRequestDto.getPassword());

        User user = User.builder()
                .loginId(registerRequestDto.getLoginId())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .nickname(registerRequestDto.getNickname())
                .username(registerRequestDto.getUsername())
                .sex(registerRequestDto.getSex())
                .email(registerRequestDto.getEmail())
                .phoneNumber(registerRequestDto.getPhoneNumber())
                .birthDate(registerRequestDto.getBirthdate())
                .role(Role.USER) // 회원 가입 시 일반 사용자로 가입.
                .provider(AuthProvider.LOCAL) // 소셜 로그인이 아니므로 LOCAL
                .build();

        return  UserResponseDto.fromEntity(userRepository.save(user));
    }

    // 로그인
    public UserResponseDto login(LoginRequestDto dto){
        
        // 사용자 아이디 조회
        User user = userRepository.findByLoginId(dto.getLoginId())
                .orElseThrow(()-> new IllegalArgumentException("아이디가 존재하지 않습니다."));

        // 비밀번호 조회
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return UserResponseDto.fromEntity(user);
    }

    // 비밀번호 변경
    public String updatePw(FindPwRequestDto dto, String newPassword) {

        // 사용자 조회
        User user = userRepository.findByUsernameAndEmail(dto.getUsername(),dto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));

        // 🔒 비밀번호 유효성 검사
        validatePassword(newPassword);

        // 비밀번호 암호화 후 업데이트
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // 저장
        userRepository.save(user);

        return "비밀번호가 성공적으로 변경되었습니다.";
    }

    // 아이디 찾기
    public String findId(FindIdRequestDto dto){

        // 사용자 조회
        User user = userRepository.findByUsernameAndEmail(dto.getUsername(),dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));

        return user.getLoginId();
    }

//    // 비밀번호 찾기 (이메일 - 임시 비밀번호)
//    public String findPwEmail(FindPwEmailRequestDto dto){
//
//        // 사용자 조회
//        User user = userRepository.findByLoginIdAndUsernameAndEmail(dto.getLoginId(), dto.getUsername(),dto.getEmail())
//                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));
//
//        // 임시 비밀번호 생성 및 DB에 임시 비밀번호 저장
//        String tempPassword = UUID.randomUUID().toString().substring(0,8);
//        user.setPassword(passwordEncoder.encode(tempPassword));
//
//        userRepository.save(user);
//
//        // 이메일 내용
//        String subject = user.getUsername() + "님, 안녕하세요. 임시 비밀번호 안내입니다.";
//        String bodyText = "안녕하세요, " + user.getUsername() + "님. \n\n" +
//                      "임시 비밀번호는 [ " + tempPassword + " ] 입니다.\n" +
//                      "로그인 후 반드시 비밀번호를 변경해 주세요.";
//
//        try {
//            if (user.getEmail().endsWith("@gmail.com")) {
//                gmailService.sendEmail(user.getEmail(), subject, bodyText);
//            } else if (user.getEmail().endsWith("@naver.com")) {
//                naverMailService.sendEmail(user.getEmail(), subject, bodyText);
//            } else {
//                throw new IllegalArgumentException("지원하지 않는 이메일 도메인입니다.");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("이메일 전송 실패: " + e.getMessage());
//        }
//        return "임시 비밀번호가 이메일로 발송되었습니다. 임시 비밀번호: " + tempPassword;
//    }

    // 인증번호 요청 (이메일 - 아이디 찾기, 비밀번호 찾기)
    public String emailVerificationCode(VerificationCodeRequestDto dto){

        // 사용자 조회
        User user = userRepository.findByUsernameAndEmail(dto.getUsername(),dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));

        // 인증번호 생성
        String code = generateCode();

        // 이메일 내용
        String subject = user.getUsername() + "님, 안녕하세요. 인증번호 안내입니다.";
        String bodyText = "안녕하세요, " + user.getUsername() + "님. \n\n" +
                "인증번호는 [ " + code + " ] 입니다.\n";

        try {
            if (user.getEmail().endsWith("@gmail.com")) {
                gmailService.sendEmail(user.getEmail(), subject, bodyText);
            } else if (user.getEmail().endsWith("@naver.com")) {
                naverMailService.sendEmail(user.getEmail(), subject, bodyText);
            } else {
                throw new IllegalArgumentException("지원하지 않는 이메일 도메인입니다.");
            }

            // 5분간 유효
            verificationCodes.put(user.getEmail(), code);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    verificationCodes.remove(user.getEmail());
                }
            }, 5 * 60 * 1000);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("이메일 전송 실패: " + e.getMessage());
        }

        return "인증번호가 이메일로 전송되었습니다. 인증번호: " + code;
    }

    // 인증번호 요청 (전화번호 - 아이디 찾기, 비밀번호 찾기)
    public String phoneNumberVerificationCode(VerificationCodeRequestDto dto){

        // 사용자 조회
        User user = userRepository.findByUsernameAndPhoneNumber(dto.getUsername(),dto.getPhoneNumber())
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다."));

        // 인증번호 생성
        String code = generateCode();

        // 문자 내용
        String message = "[Web발신]\n" + "인증번호[ " + code +" ]" + "타인에게 절대 알려주지 마세요.";

        awsSmsService.sendSms(user.getPhoneNumber(), message);

        // 5분간 유효
        verificationCodes.put(user.getPhoneNumber(), code);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                verificationCodes.remove(user.getPhoneNumber());
            }
        }, 5 * 60 * 1000);

        return "인증번호가 전화번호로 전송되었습니다. 인증번호: " + code;
    }

    // 🔒 비밀번호 유효성 검사
    private void validatePassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,}$";
        if (!Pattern.matches(regex, password)) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며, 대소문자/숫자/특수문자를 포함해야 합니다.");
        }
    }

    // ✅ 6자리 난수 생성
    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

}
