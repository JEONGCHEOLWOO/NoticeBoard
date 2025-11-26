package com.example.NoticeBoard.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;

@Service
public class TwilioSmsService {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    // 생성자
    public TwilioSmsService() {
        Dotenv dotenv = Dotenv.load();
        this.accountSid = dotenv.get("TWILIO_ACCOUNT_SID");
        this.authToken = dotenv.get("TWILIO_AUTH_TOKEN");
        this.fromNumber = dotenv.get("TWILIO_PHONE_NUMBER");
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String to, String content) {
        try {
            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(to),      // 수신자 번호 (예: +821012345678)
                    new com.twilio.type.PhoneNumber(fromNumber), // Twilio 발신 번호
                    content
            ).create();

            System.out.println("✅ SMS 전송 성공 - SID: " + message.getSid());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("문자 전송 실패: " + e.getMessage());
        }
    }
}