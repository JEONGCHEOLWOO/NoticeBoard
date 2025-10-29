package com.example.NoticeBoard;

import com.example.NoticeBoard.service.GmailService;
import com.example.NoticeBoard.service.NaverMailService;
import jakarta.mail.MessagingException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Bean
    public GmailService gmailService() throws Exception {
        return new GmailService();  // GmailService 내부에서 .env 사용
    }

    @Bean
    public NaverMailService naverMailService() throws MessagingException {
        return new NaverMailService(); // NaverMailService 내부에서 .env 사용
    }

}
