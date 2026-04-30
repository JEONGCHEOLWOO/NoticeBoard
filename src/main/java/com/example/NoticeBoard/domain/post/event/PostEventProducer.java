package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

// kafka 이벤트 발행
@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String POST_EVENT_TOPIC = "post-event-topic";

    public void sendPostCreatedEvent(Long postId){
        PostEvent event = PostEvent.builder()
                        .eventType(PostEventType.CREATE)
                        .postId(postId)
                        .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
        log.info("게시글 이벤트 발행: type={}, postId={}", event.getEventType(), postId);
    }

    public void sendPostUpdateEvent(Long postId){
        PostEvent event = PostEvent.builder()
                .eventType(PostEventType.UPDATE)
                .postId(postId)
                .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
        log.info("게시글 이벤트 발행: type={}, postId={}", event.getEventType(), postId);
    }

    public void sendPostDeleteEvent(Long postId){
        PostEvent event = PostEvent.builder()
                .eventType(PostEventType.DELETE)
                .postId(postId)
                .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
        log.info("게시글 이벤트 발행: type={}, postId={}", event.getEventType(), postId);
    }

    public void sendPostDeleteBatchEvent(List<Long> postIds){
        PostEvent event = PostEvent.builder()
                .eventType(PostEventType.DELETE_BATCH)
                .postIds(postIds)
                .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
    }

}
