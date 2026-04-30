package com.example.NoticeBoard.domain.comment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String COMMENT_COUNT_KEY = "post:comment:count:";
    private static final String IDEMPOTENT_KEY = "comment:event:processed:";

    @KafkaListener(topics = "comment-event-topic", groupId = "comment-group", concurrency = "3")
    public void consume(CommentEvent event){

        log.info("댓글 CUD Kafka 이벤트 수신 type={}", event.getEventType());

        // 멱등성 처리
        String eventKey = IDEMPOTENT_KEY + event.getEventType() + ":" + event.getCommentId();

        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(eventKey, "1");

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("중복 이벤트 무시 {}", eventKey);
            return;
        }

        switch (event.getEventType()){
            case CREATE:
                increaseCommentCount(event.getPostId());
                break;
            case DELETE:
                decreaseCommentCount(event.getPostId());
                break;
            case UPDATE:
                break;
            case DELETE_BATCH:
                break;
        }
    }

    private void increaseCommentCount(Long postId){
        redisTemplate.opsForValue().increment(COMMENT_COUNT_KEY + postId);
        log.info("Redis post:comment:count:{postId} INCR 완료: postId={}, key={}", postId, COMMENT_COUNT_KEY);
    }

    private void decreaseCommentCount(Long postId){
        redisTemplate.opsForValue().decrement(COMMENT_COUNT_KEY + postId);
        log.info("Redis post:comment:count:{postId} DECR 완료: postId={}, key={}", postId, COMMENT_COUNT_KEY);
    }
}

