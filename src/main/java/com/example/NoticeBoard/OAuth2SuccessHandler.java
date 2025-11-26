package com.example.NoticeBoard;

import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    // OAuth2 로그인 성공 시 실행.
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        // OAuth2User 가져오기
        DefaultOAuth2User oAuthUser = (DefaultOAuth2User) authentication.getPrincipal();

        // 이메일, provider  가져오기
        String email = extractEmail(oAuthUser);
        String providerName = extractProvider(oAuthUser);

        // DB에서 사용자 조회 (findByEmail)
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));

        // JWT 생성
        String token = jwtProvider.generateToken(authentication);

        // 프론트엔드 리다이렉트 URL
        String userName = user.getUsername();
        String role = user.getRole().name();

        // URL 파라미터 인코딩
//        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedUsername = URLEncoder.encode(userName, StandardCharsets.UTF_8);
        String encodedProvider = URLEncoder.encode(providerName, StandardCharsets.UTF_8);
        String encodedRole = URLEncoder.encode(role, StandardCharsets.UTF_8);

        // 프론트엔드 리다이렉트 URL 구성
        String redirectUri =
                "http://localhost:8080/oauth-success"
                        + "?token=" + token
                        + "&email=" + email
                        + "&userName=" + encodedUsername
                        + "&provider=" + encodedProvider
                        + "&role=" + encodedRole;

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    // Email 추출
    private String extractEmail(DefaultOAuth2User user) {
        if (user.getAttributes().containsKey("response")) {
            return ((Map<String, Object>) user.getAttributes().get("response")).get("email").toString();
        }
        return user.getAttribute("email");
    }

    // provider 추출
    private String extractProvider(DefaultOAuth2User user) {
        if (user.getAttributes().containsKey("provider")) {
            return user.getAttribute("provider");
        }
        return "LOCAL"; // 기본값
    }
}
