package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikeConsumer {

    private final StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "post-like-topic")
    public void consume(String message){

        String[] parts = message.split(":");

        long postId = Long.parseLong(parts[0]);
        String type = parts[1];

        String key = "post:like:count:" + postId;

        if(type.equals("LIKE")){
            // INCR post:like:count:postId -> INCR post:like:count:12 -> 게시글 12 좋아요 수 증가.
            // 여기서 Redis의 String 명령어 INCR 실행.
            redisTemplate.opsForValue().increment(key);
        }

        if(type.equals("UNLIKE")){
            // DECR post:like:count:postId -> DECR post:like:count:12 -> 게시글 12 좋아요 수 감소.
            // 여기서 Redis의 String 명령어 DECR 실행.
            redisTemplate.opsForValue().decrement(key);
        }
    }
}
