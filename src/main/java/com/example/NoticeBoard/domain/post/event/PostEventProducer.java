package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// kafka 이벤트 발행
@Service
@RequiredArgsConstructor
public class PostEventProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;
    private static final String TOPIC = "post-event-topic";

    public void sendPostCreatedEvent(Long postId){
        kafkaTemplate.send(TOPIC, "CREATE", postId);
    }

    public void sendPostUpdateEvent(Long postId){
        kafkaTemplate.send(TOPIC, "UPDATE", postId);
    }

    public void sendPostDeleteEvent(Long postId){
        kafkaTemplate.send(TOPIC, "DELETE", postId);
    }
}
