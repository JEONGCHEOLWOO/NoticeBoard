package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Kafka 이벤트 발행
@Service
@RequiredArgsConstructor
public class PostLikeProducer {

    public final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String POST_LIKE_TOPIC = "post-like-topic";

    public void sendLikeEvent(Long postId) {
        kafkaTemplate.send(POST_LIKE_TOPIC, postId + ":LIKE");
    }

    public void sendUnlikeEvent(Long postId) {
        kafkaTemplate.send(POST_LIKE_TOPIC, postId + ":UNLIKE");
    }
}
