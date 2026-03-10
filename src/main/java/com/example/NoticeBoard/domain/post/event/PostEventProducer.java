package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// kafka 이벤트 발행
@Service
@RequiredArgsConstructor
public class PostEventProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public void sendPostCreatedEvent(Long postId){
        kafkaTemplate.send("post-evnet-topic", postId);
    }

    public void sendPostUpdateEvent(Long postId){
        kafkaTemplate.send("post-evnet-topic", postId);
    }

    public void sendPostDeleteEvent(Long postId){
        kafkaTemplate.send("post-evnet-topic", postId);
    }
}
