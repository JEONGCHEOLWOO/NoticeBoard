package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Kafka 이벤트 발행
@Service
@RequiredArgsConstructor
public class PostLikeProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendLikeEvent(Long postId) {
        kafkaTemplate.send("post-like-topic", postId + ":LIKE");
    }

    public void sendUnlikeEvent(Long postId) {
        kafkaTemplate.send("post-like-topic", postId + ":UNLIKE");
    }
}
