package com.example.NoticeBoard.service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class NaverMailService {

    private final String username;
    private final String password;
    private final Session session;

    // Jakarta Mail API - SMTP 방식
    public NaverMailService() {
        Dotenv dotenv = Dotenv.load();

        this.username = dotenv.get("NAVER_ID");
        this.password = dotenv.get("NAVER_PASSWORD");

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.naver.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        this.session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    // 이메일 전송
    public void sendEmail(String to, String subject, String body) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }
}
