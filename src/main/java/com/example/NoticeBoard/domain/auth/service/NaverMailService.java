package com.example.NoticeBoard.domain.auth.service;

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
    private final String host = "smtp.naver.com";
    private final int port = 465;
    // Jakarta Mail API - SMTP 방식
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
