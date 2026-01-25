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

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",
                        "/mail/**",
                        "/error/**",
                        "/css/**",
                        "/js/**"
                );
    }

}
