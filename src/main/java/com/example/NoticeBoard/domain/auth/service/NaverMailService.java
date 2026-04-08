package com.example.NoticeBoard.domain.auth.service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.Properties;

// 아이디 찾기, 비밀번호 찾기에서 이메일 인증을 위한 클래스 - NaverMail
@Service
public class NaverMailService {

    private final String username;
    private final String password;
    private final String host = "smtp.naver.com";
    private final int port = 465;
    
    // Jakarta Mail API - SMTP 방식
    // SMTP란?
    // Simple Mail Transfer Protocol 단순 전자우편 전송 규약으로 인터넷을 통해 이메일을 보낼 때 사용하는 표준 프로토콜
    // 스팸 및 무단 사용을 방지하기 위해 사용자는 메일 서버에 로그인하여 인증을 거쳐야만 메일을 보낼 수 있는 구조
    // 포트는 587(TLS)나 465(SSL) 포트를 사용. TLS - Transport Layer Securiy(전송 계층 보안),  SSL - Secure Sockets Layer(보안 소켓 계층)
    public NaverMailService() {
        Dotenv dotenv = Dotenv.load();
        this.username = dotenv.get("NAVER_ID");
        this.password = dotenv.get("NAVER_PASSWORD");
    }

    // 이메일 전송
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
