package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostLikeConsumer {

    private final StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "post-like-topic")
    public void consume(String message){

        log.info("게시글 좋아요 kafka 이벤트 수신: message={}", message);

        String[] parts = message.split(":");

        long postId = Long.parseLong(parts[0]);
        String type = parts[1];

        log.debug("파싱 완료: postId={}, type={}", postId, type);

        String key = "post:like:count:" + postId;

        if(type.equals("LIKE")){
            // INCR post:like:count:{postId} -> INCR post:like:count:12 -> 게시글 12 좋아요 수 증가.
            // 여기서 Redis의 String 명령어 INCR 실행.
            redisTemplate.opsForValue().increment(key);
            log.info("Redis post:like:count:{postId} INCR 완료: postId={}, key={}", postId, key);
        }
//
//        if(type.equals("UNLIKE")){
//            // DECR post:like:count:{postId} -> DECR post:like:count:12 -> 게시글 12 좋아요 수 감소.
//            // 여기서 Redis의 String 명령어 DECR 실행.
//            redisTemplate.opsForValue().decrement(key);
//            log.info("Redis post:like:count:{postId} DECR 완료: postId={}, key={}", postId, key);
//        }
    }
}
