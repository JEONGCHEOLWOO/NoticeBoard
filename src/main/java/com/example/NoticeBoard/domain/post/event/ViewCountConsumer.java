package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// kafka 이벤트 처리 클래스
@Component
@RequiredArgsConstructor
public class ViewCountConsumer {

    private final StringRedisTemplate redisTemplate;

    // kafka Topic에 들어온 메세지를 읽어서 처리하는 역할
    @KafkaListener(topics = "post-view-topic")
    public void consume(Long postId){
        String key = "post:view:" + postId;
        // INCR post:view:postId -> INCR post:view:12 -> 게시글 12의 조회수 증가.
        // 여기서 Redis의 String 명령어 INCR 실행.
        redisTemplate.opsForValue().increment(key);
    }
}
