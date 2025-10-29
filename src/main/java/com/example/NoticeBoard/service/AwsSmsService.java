package com.example.NoticeBoard.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class AwsSmsService {

    private final SnsClient snsClient;

    // 생성자
    public AwsSmsService() {

        Dotenv dotenv = Dotenv.load();
        String region = dotenv.get("AWS_REGION");
        String accessKeyId = dotenv.get("AWS_ACCESS_KEY_ID");
        String secretAccessKey = dotenv.get("AWS_SECRET_ACCESS_KEY");

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    // AWS SNS에 있는 SMS 메소드로 메세지 전송
    public void sendSms(String phoneNumberE164, String message) {

        PublishRequest request = PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumberE164)
                .build();

        snsClient.publish(request);
    }

}
