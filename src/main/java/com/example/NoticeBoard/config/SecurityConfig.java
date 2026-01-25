package com.example.NoticeBoard.config;

import com.example.NoticeBoard.component.OAuth2SuccessHandler;
import com.example.NoticeBoard.service.OAuth2LoginService;
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 소셜 로그인용
        http
            .csrf(csrf -> csrf.disable())
            .cors(withDefaults())
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
