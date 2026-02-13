package com.example.NoticeBoard.global.config;

import com.example.NoticeBoard.domain.auth.service.GmailService;
import com.example.NoticeBoard.domain.auth.service.NaverMailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Bean
    public GmailService gmailService() throws Exception {
        return new GmailService();  // GmailService 내부에서 .env 사용
    }

    @Bean
    public NaverMailService naverMailService() {
        return new NaverMailService(); // NaverMailService 내부에서 .env 사용
    }

}
