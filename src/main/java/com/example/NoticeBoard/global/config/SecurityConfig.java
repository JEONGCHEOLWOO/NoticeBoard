package com.example.NoticeBoard.global.config;

import com.example.NoticeBoard.domain.auth.handler.OAuth2SuccessHandler;
import com.example.NoticeBoard.domain.auth.service.OAuth2LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2LoginService oAuth2LoginService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); // BCrypt 해시 알고리즘 사용 
    }

    // HTTP 요청 보안 정책 정의
    // 모든 프로젝트에서 필수적으로 사용되고, 어떤 URL을 공개하고 인증을 통해 허가할지 설정.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable()) // CSRF 설정 -> REST API는 토근 기반 인증이라 CSRF 불필요.
            .cors(withDefaults()) // WebConfig의 CORS 설정 사용.
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/oauth2/**", "/login/**", "/oauth-success").permitAll()
                    .requestMatchers("/posts/search/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2LoginService))
                .authorizationEndpoint(endpoint ->
                    endpoint.baseUri("/oauth2/authorization")
                ).successHandler(oAuth2SuccessHandler)
            );

        return http.build();
    }
}
