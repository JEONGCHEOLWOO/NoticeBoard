package com.example.NoticeBoard.domain.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    // HandlerInterceptor 인터페이스에는 preHandle(), postHandle(), afterCompletion() 총 3가지 메소드가 있다.
    // preHandle() 메소드는 Controller 메소드 호출 전에 실행 되고,
    // postHandle() 메소드는 Controller 메소드 호출 이후, View 렌더링 전(Dispatcher Servlet이 화면을 처리하기 전)에 실행 되고,
    // afterCompletion() 메소드는 Dispatcher Servlet 의 화면 처리(Veiw)가 완료된 상태에서 실행된다.   
    // Dispatcher Servlet 은 모든 요청의 출입구, Spring MVC의 프론트 컨트롤러의 역활

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        log.info("REQUSET [{}] {}", request.getMethod(), request.getRequestURI());
        return true;
    }
}
