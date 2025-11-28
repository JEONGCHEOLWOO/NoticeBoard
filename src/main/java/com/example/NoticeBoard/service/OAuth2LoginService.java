package com.example.NoticeBoard.service;

import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.enumeration.AuthProvider;
import com.example.NoticeBoard.enumeration.Role;
import com.example.NoticeBoard.enumeration.Sex;
import com.example.NoticeBoard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService extends DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId().toLowerCase();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 공통 변수 선언
        String email = null;
        String name = null;
        String nickname = null;
        String gender = null;
        String birthday = null;
        String phoneNumber = null;
        AuthProvider authProvider = null;
        String provider_id = null;

        // 소셜 로그인 타입별 처리 - Naver, Kakao, Google 확인완료.
        switch (provider) {
            case "naver":
                Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
                email = (String) naverResponse.get("email");
                name = (String) naverResponse.get("name");
                gender = (String) naverResponse.get("gender");
                birthday = (String) naverResponse.get("birthday");
                authProvider = AuthProvider.NAVER;
                break;
            case "google":
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                authProvider = AuthProvider.GOOGLE;
                break;
            case "kakao": // profile_nickname, name, account_email, gender, birthday, birthyear, phone_number, profile_image
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
                email = (String) kakaoAccount.get("email");
                name = (String) kakaoAccount.get("name");
                nickname = (String) kakaoProfile.get("nickname");
                gender = (String) kakaoProfile.get("gender");
                birthday = (String) kakaoProfile.get("birthday") + kakaoProfile.get("birthyear");
                phoneNumber = (String) kakaoProfile.get("phone_number");
                authProvider = AuthProvider.KAKAO;
                break;
            case "facebook": // id, name, email, gender, birthday, profile(공개 프로필)
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                nickname = (String) attributes.get("name"); // nickname이 따로 없으면 name을 nickname으로 설정.
                gender = (String) attributes.get("gender");
                birthday = (String) attributes.get("birthday");
                provider_id = (String) attributes.get("id");
                authProvider = AuthProvider.FACEBOOK;
                break;
            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }

        // 성별 처리
        Sex sex;
        if ("M".equalsIgnoreCase(gender) || "male".equalsIgnoreCase(gender)) sex = Sex.MALE;
        else if ("F".equalsIgnoreCase(gender) || "female".equalsIgnoreCase(gender)) sex = Sex.FEMALE;
        else sex = Sex.UNKNOWN;

        // 기본 역할
        Role role = Role.USER;

        // DB 조회 후 없으면 새로 저장 - DB에 저장 하는지 확인 완료.
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder()
                    .loginId(email)
                    .nickname(nickname)
                    .username(name)
                    .password("") // 소셜 로그인 시 임의 패스워드
                    .sex(sex)
                    .email(email)
                    .phoneNumber("+821025278661") // 선택
                    .birthDate(birthday) // 선택
                    .role(role)
                    .provider(authProvider)
                    .providerId(provider_id)
                    .build();
            userRepository.save(user);
        }

        // OAuth2User 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(role.name())),
                Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getUsername(),
                        "provider", authProvider.name()
                ),
                "email"
        );
    }
}
