package com.example.NoticeBoard.config;

import com.example.NoticeBoard.Interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
// WebConfig는 웹(Spring MVC) 동작 방식을 설정하는 클래스
// CORS 설정, Interceptor, ArgumentResolver, HttpMessageConverter, 정적 리소스 매핑, 포맷터(날짜, enum 등)이 들어간다.
public class WebConfig implements WebMvcConfigurer {

    // CORS(Cross-Origin Resource Sharing, 교차 출처 리소스 공유) 설정 -> Front에서 사용하는 도메인과 Back에서 사용하는 도메인의 주소가 다를 떄 요청과 응답을 받을 수 있도록 허용하는 설정
    // http://localhost:3000/post/1 -> http: 는 프로토콜(protocol), localhost 는 도메인(hostname), 3000 은 포트(port), post/1 은 경로(pathname)
    // 출처(Origin) - 프로토콜 + 도메인 + 포트를 말함. (http://localhost:3000)
    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**") // 어떤 경로에 적용할지
                .allowedOrigins("http://localhost:3000") // 허용할 출처(Origin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // 허용할 HTTP 메소드
                .allowedHeaders("*") // 모든 HTTP 헤더 허용
                .allowCredentials(true) // 쿠기/인증 정보 포함 허용
                .maxAge(3600); // preflight 캐시 시간 (1시간)
    }

    // Interceptor 설정 -> 컨트롤러를 호출하기 전과 후에 요청이나 응답을 가로채 미리 정의된 로직(인증, 로깅 등)을 적용하는 기능, API 호출에 대한 로깅이나 감사를 진행.
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/**")     // 적용할 경로 (모든 경로)
                .excludePathPatterns(       // 제외할 경로
                        "/auth/**",
                        "/mail/**",
                        "/error/**",
                        "/css/**",
                        "/js/**"
                );
    }

}
