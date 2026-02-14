package com.example.NoticeBoard.domain.user.service;

import com.example.NoticeBoard.domain.auth.dto.VerificationCodeRequestDto;
import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.repository.CommentRepository;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.user.dto.FindIdRequestDto;
import com.example.NoticeBoard.domain.user.dto.FindPwRequestDto;
import com.example.NoticeBoard.global.Exception.BusinessException;
import com.example.NoticeBoard.domain.user.dto.UserRegisterRequestDto;
import com.example.NoticeBoard.domain.user.dto.UserResponseDto;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import com.example.NoticeBoard.domain.user.entity.User;
import com.example.NoticeBoard.global.enumeration.AuthProvider;
import com.example.NoticeBoard.global.enumeration.ErrorCode;
import com.example.NoticeBoard.global.enumeration.Role;
import com.example.NoticeBoard.domain.auth.service.GmailService;
import com.example.NoticeBoard.domain.auth.service.NaverMailService;
import com.example.NoticeBoard.domain.auth.service.TwilioSmsService;
import com.example.NoticeBoard.domain.auth.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final GmailService gmailService;
    private final NaverMailService naverMailService;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화(BCrypt)
    private final TwilioSmsService twilioSmsService;

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // 회원가입 - postman으로 예외처리, 유효성 검사 전부 테스트 완료
    public UserResponseDto register(UserRegisterRequestDto registerRequestDto){

        try {
            if (userRepository.existsByLoginId(registerRequestDto.getLoginId())) {
                throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
            }
            if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
            if (userRepository.existsByNickname(registerRequestDto.getNickname())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
            if (userRepository.existsByPhoneNumber(registerRequestDto.getPhoneNumber())) {
                throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
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

            return UserResponseDto.fromEntity(userRepository.save(user));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 로그인 - postman으로 예외처리 전부 확인 완료.
    public UserResponseDto login(LoginRequestDto dto){

        try{
            // 사용자 아이디 조회
            User user = userRepository.findByLoginId(dto.getLoginId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 비밀번호 조회
            if(!passwordEncoder.matches(dto.getPassword(), user.getPassword())){
                throw new BusinessException(ErrorCode.INVALID_PASSWORD);
            }

            return UserResponseDto.fromEntity(user);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 비밀번호 변경 - postman으로 예외처리, 유효성 검사 확인 완료.
    public String updatePw(FindPwRequestDto dto, String newPassword) {

        try {
            // 사용자 조회
            User user = userRepository.findByLoginIdAndUsername(dto.getLoginId(), dto.getUsername())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 🔒 비밀번호 유효성 검사
            validatePassword(newPassword);

            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                throw new BusinessException(ErrorCode.PASSWORD_SAME_AS_OLD);
            }

            // 비밀번호 암호화 후 업데이트
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return "비밀번호가 성공적으로 변경되었습니다.";

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 아이디 찾기 - 이메일로 찾기, 전화번호로 찾기, 예외처리 postman으로 확인 완료.
    public String findId(FindIdRequestDto dto) {
        try {
            User user;

            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                user = userRepository.findByUsernameAndPhoneNumber(dto.getUsername(), dto.getPhoneNumber())
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            } else {
                user = userRepository.findByUsernameAndEmail(dto.getUsername(), dto.getEmail())
                        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            }

            return user.getLoginId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
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

    // 이메일로 인증번호 요청 (아이디 찾기, 비밀번호 찾기) - postman으로 예외처리 확인 완료.
    public String emailVerificationCode(VerificationCodeRequestDto dto){

        try {
            // 사용자 조회
            User user = userRepository.findByUsernameAndEmail(dto.getUsername(),dto.getEmail())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 인증번호 생성
            String code = generateCode();
            String subject = user.getUsername() + "님, 안녕하세요. 인증번호 안내입니다.";
            String bodyText = "안녕하세요, " + user.getUsername() + "님. \n\n" +
                    "인증번호는 [ " + code + " ] 입니다.\n";


            if (user.getEmail().endsWith("@gmail.com")) {
                gmailService.sendEmail(user.getEmail(), subject, bodyText);
            } else if (user.getEmail().endsWith("@naver.com")) {
                naverMailService.sendEmail(user.getEmail(), subject, bodyText);
            } else {
                throw new BusinessException(ErrorCode.UNSUPPORTED_EMAIL_DOMAIN);
            }

            // 5분간 유효
            verificationCodes.put(user.getEmail(), code);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    verificationCodes.remove(user.getEmail());
                }
            }, 5 * 60 * 1000);

            return "인증번호가 이메일로 전송되었습니다. 인증번호: " + code;

        } catch (BusinessException e) {
            log.warn("[login] 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[login] 내부 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 전화번호로 인증번호 요청 (아이디 찾기, 비밀번호 찾기)
    public String phoneNumberVerificationCode(VerificationCodeRequestDto dto) {

        try{
            // 사용자 조회
            User user = userRepository.findByUsernameAndPhoneNumber(dto.getUsername(), dto.getPhoneNumber())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 인증번호 생성
            String code = generateCode();
            String message = "[Web발신]\n" + "인증번호[ " + code + " ]" + "타인에게 절대 알려주지 마세요.";

            twilioSmsService.sendSms(user.getPhoneNumber(), message);

            // 5분간 유효
            verificationCodes.put(user.getPhoneNumber(), code);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    verificationCodes.remove(user.getPhoneNumber());
                }
            }, 5 * 60 * 1000);

            return "인증번호가 전화번호로 전송되었습니다. 인증번호: " + code;
        } catch (BusinessException e) {
            log.warn("[login] 비즈니스 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[login] 내부 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 🔒 비밀번호 유효성 검사
    private void validatePassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,}$";
        if (!Pattern.matches(regex, password)) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

    // ✅ 6자리 난수 생성
    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // 사용자 상세 정보 조회
    public UserDetailDto getUserDetail(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        List<Post> posts = postRepository.findByUserId(userId);
        List<Comment> comments = commentRepository.findByUserId(userId);

        return UserDetailDto.builder()
                .user(user)
                .posts(posts)
                .comments(comments)
                .postCount(posts.size())
                .commentCount(comments.size())
                .build();
    }
}
