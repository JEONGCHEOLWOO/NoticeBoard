package com.example.NoticeBoard.domain.auth.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// 전화번호 인증을 위해 인증번호를 문자로 발송하는 클래스 - TwilioSMS API
@Service
public class TwilioSmsService {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    public TwilioSmsService(
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.phone-number}") String fromNumber
    ) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;

        Twilio.init(accountSid, authToken);
    }

    // 문자로 인증번호 전송
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