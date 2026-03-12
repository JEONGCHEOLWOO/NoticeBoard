package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikeConsumer {

    private final StringRedisTemplate stringRedisTemplate;

    @KafkaListener(topics = "post-like-topic")
    public void consume(String message){

        String[] parts = message.split(":");

        long postId = Long.parseLong(parts[0]);
        String type = parts[1];

        String key = "post:like:count:" + postId;

        if(type.equals("LIKE")){
            stringRedisTemplate.opsForValue().increment(key);
        }

        if(type.equals("UNLIKE")){
            stringRedisTemplate.opsForValue().decrement(key);
        }
    }
}
