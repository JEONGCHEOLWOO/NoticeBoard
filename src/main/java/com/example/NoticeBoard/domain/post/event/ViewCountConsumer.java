package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// kafka 이벤트 처리 클래스
@Component
@RequiredArgsConstructor
public class ViewCountConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    // kafka Topic에 들어온 메세지를 읽어서 처리하는 역할
    @KafkaListener(topics = "post-view-topic")
    public void consume(Long postId){
        String key = "post:view:" + postId;
        redisTemplate.opsForValue().increment(key);
    }
}
