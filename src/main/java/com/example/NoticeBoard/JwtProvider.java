package com.example.NoticeBoard;

import java.util.Date;

import com.google.api.client.util.Value;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

// 웹 애플리케이션에서 사용자 인증 및 권한 부여를 위해 사용되는 전자 서명된 JSON 토큰
@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private final long validityInMilliseconds = 3600000L; // 1시간

    public String generateToken(Authentication authentication) {
        String email = extractEmailFromPrincipal(authentication);
        Claims claims = Jwts.claims().setSubject(email);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds); // 토큰 만료 시간
        System.out.println("secretKey: " + secretKey);
        System.out.println("SignatureAlgorithm.HS256: " + SignatureAlgorithm.HS256);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now) // 토큰 만료 시간 설정
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
    private String extractEmailFromPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof DefaultOAuth2User) {
            DefaultOAuth2User user = (DefaultOAuth2User) principal;
            // 네이버인 경우 response 안에 email이 있고, 다른 제공자는 attributes에 바로 email이 있을 수 있음
            if (user.getAttributes().containsKey("response")) {
                Object resp = user.getAttributes().get("response");
                if (resp instanceof java.util.Map) {
                    Object email = ((java.util.Map<?, ?>) resp).get("email");
                    return email != null ? email.toString() : null;
                }
            }
            Object email = user.getAttribute("email");
            return email != null ? email.toString() : null;
        }
        return null;
    }
}
