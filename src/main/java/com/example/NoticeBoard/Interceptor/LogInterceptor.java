package com.example.NoticeBoard.Interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    // Controller 메소드 호출 전 
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler){
        log.info("REQUSET [{}] {}", request.getMethod(), request.getRequestURI());
        return true;
    }
}
