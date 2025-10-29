package com.example.NoticeBoard.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.io.*;

@Service
public class GmailService {
    private Gmail gmail;

    public GmailService() throws Exception {
        Dotenv dotenv = Dotenv.load();

        String clientId = dotenv.get("GMAIL_CLIENT_ID");
        String clientSecret = dotenv.get("GMAIL_CLIENT_SECRET");
        String refreshToken = dotenv.get("GMAIL_REFRESH_TOKEN");

        // OAuth2 인증 생성
        Credential credential = new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .build()
                .setRefreshToken(refreshToken);

        // Gmail API 클라이언트 객체 생성
        this.gmail = new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        )
                .setApplicationName("NoticeBoard")
                .build();
    }

    // 이메일 전송
    public void sendEmail(String to, String subject, String bodyText) throws Exception {
        
        // "me" 는 OAuth 2.0 인증된 구글 사용자, Gmail API가 자동으로 내 구글 계정으로 대체하는 예약어, 내부적으로는 실제로 "admin123@gmail.com" 으로 되어있음.
        MimeMessage mimeMessage = createEmail(to, subject, bodyText);
        Message message = createMessageWithEmail(mimeMessage);
        
        try {
            gmail.users().messages().send("me", message).execute();
            System.out.println("✅ 이메일 발송 완료: " + to);
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            System.err.println("⚠️ 이메일 발송 실패: " + error);
            throw e;
        }
    }

    // MIME(Multipurpose Internet Mail Extensions) 메세지 생성
    // MIME - 텍스트, 이미지, 파일, HTML 등 다양한 형식을 한꺼번에 묶어서 전송하기 위한 표준 규격
    private MimeMessage createEmail(String toEmailAddress, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress("me"));
        email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(toEmailAddress));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    // Gmail 전송용 Message 객체로 변환
    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }
}

