package com.example.NoticeBoard.service;

import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.enumeration.AuthProvider;
import com.example.NoticeBoard.enumeration.Role;
import com.example.NoticeBoard.enumeration.Sex;
import com.example.NoticeBoard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService extends DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private Logger log;


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
            case "google": // OAuth2 API - name, email People API - gender, birthday, phoneNumber - 프로필에서 정보가 누락되어 있으면 error발생.(해결 완료)
                String accessToken = userRequest.getAccessToken().getTokenValue();
                Map<String, Object> person = getPerson(accessToken);
                Map<String, String> info = extractInfo(person);
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                nickname = (String) attributes.get("name");
                gender = info.get("gender");
                birthday = info.get("birthday");
                phoneNumber = info.get("phoneNumber");
                provider_id = email;
                authProvider = AuthProvider.GOOGLE;
                System.out.println("email: " + email + " name: " + name + " nickname: " + nickname + " gender: " + gender + " birthday: " + birthday + " phoneNumber: " + phoneNumber);
                break;
            case "kakao": // profile_nickname, name, account_email, gender(male, female), birthday, birthyear, phone_number, profile_image
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
                email = (String) kakaoAccount.get("email");
                name = (String) kakaoAccount.get("name");
                nickname = (String) kakaoProfile.get("nickname");
                gender = (String) kakaoAccount.get("gender");
                birthday = (String) kakaoAccount.get("birthyear") + kakaoAccount.get("birthday");
                phoneNumber = ((String) kakaoAccount.get("phone_number")).replaceAll("[\\s-]",""); // +82 010-1234-4567 형태를 +820123456789 로 변경. 공백,하이픈 제거
                provider_id = email;
                authProvider = AuthProvider.KAKAO;
                break;
            case "facebook": // id, name, email, gender(M, F), birthday, profile(공개 프로필) - phoneNumber 못받음.
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                nickname = (String) attributes.get("name"); // nickname이 따로 없으면 name을 nickname으로 설정.
                gender = (String) attributes.get("gender");
                String[] birthArr = ((String) attributes.get("birthday")).split("/"); // 01/01/2025 형태를 20250101 로 변경.
                birthday = birthArr[2] + birthArr[0] + birthArr[1];
                provider_id = (String) attributes.get("id");
                phoneNumber = "+820123456789";
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
                    .phoneNumber(phoneNumber) // 선택
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
    private Map<String, Object> getPerson(String accessToken) {
        String url = "https://people.googleapis.com/v1/people/me"
                + "?personFields=names,emailAddresses,genders,birthdays,phoneNumbers";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        return response.getBody();
    }

    // try catch 문을 사용한 이유: try catch문을 사용하지 않으면, 사용자가 생일을 등록을 안했을 때, 성별을 설정 안했을 때, 전화번호를 등록하지 않았을 때 NullPointerException이 발생함.
    private Map<String, String> extractInfo(Map<String, Object> personJson) {
        Map<String, String> result = new HashMap<>();

        // gender
        try {
            List<Map<String, Object>> genders = (List<Map<String, Object>>) personJson.get("genders");
            if (genders != null && !genders.isEmpty()) {
                // 이런식으로 생김 : [{metadata={primary=true, source={type=PROFILE, id=116922998305670175340}}, value=male, formattedValue=Male}]
                Map<String, Object> gender = genders.get(0);
                result.put("gender", (String) gender.get("value"));
            }
        } catch (Exception e) {
            log.warn("⚠ 구글 계정 프로필에서 필요한 정보가 누락되었습니다(성별(남/여)): " + e);
        }

        // birthday
        try {
            List<Map<String, Object>> birthdays = (List<Map<String, Object>>) personJson.get("birthdays");
            if (birthdays != null && !birthdays.isEmpty()) {
                // 이런식으로 생김 : [{metadata={primary=true, source={type=PROFILE, id=116922998305670175340}}, date={year=1999, month=12, day=28}}, {metadata={source={type=ACCOUNT, id=116922998305670175340}}, date={year=1999, month=12, day=28}}]
                Map<String, Object> birthday = birthdays.get(0);
                Map<String, Object> date = (Map<String, Object>) birthday.get("date");
                if (date != null) {
                    Integer year  = (Integer) date.get("year");
                    Integer month = (Integer) date.get("month");
                    Integer day   = (Integer) date.get("day");
                    if (year != null && month != null && day != null) {
                        String s = String.format("%04d%02d%02d", year, month, day);
                        result.put("birthday", s);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("⚠ 구글 계정 프로필에서 필요한 정보가 누락되었습니다(생년월일): " + e);
        }

        // phone number
        try {
            List<Map<String, Object>> phones = (List<Map<String, Object>>) personJson.get("phoneNumbers");
            // 이런식으로 생김:  [{metadata={primary=true, verified=true, source={type=PROFILE, id=116922998305670175340}}, value=010-2527-8661, canonicalForm=+821025278661, type=mobile, formattedType=Mobile}]
            if (phones != null && !phones.isEmpty()) {
                Map<String, Object> phoneNumber = phones.get(0);
                result.put("phoneNumber", (String) phoneNumber.get("canonicalForm"));
            }
        } catch (Exception e) {
            log.warn("⚠ 구글 계정 프로필에서 필요한 정보가 누락되었습니다(전화번호): " + e);
        }

        return result;
    }
}
