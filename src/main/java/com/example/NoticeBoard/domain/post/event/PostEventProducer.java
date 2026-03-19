package com.example.NoticeBoard.domain.post.event;

import com.example.NoticeBoard.domain.post.entity.PostEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

// kafka 이벤트 발행
@Service
@RequiredArgsConstructor
public class PostEventProducer {

    private final KafkaTemplate<String, PostEvent> kafkaTemplate;
    public static final String POST_EVENT_TOPIC = "post-event-topic";

    public void sendPostCreatedEvent(Long postId){
        PostEvent event = PostEvent.builder()
                        .eventType("CREATE")
                        .postId(postId)
                        .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
    }

    public void sendPostUpdateEvent(Long postId){
        PostEvent event = PostEvent.builder()
                .eventType("UPDATE")
                .postId(postId)
                .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
    }

    public void sendPostDeleteEvent(Long postId){
        PostEvent event = PostEvent.builder()
                .eventType("DELETE")
                .postId(postId)
                .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
    }

    public void sendPostDeleteBatchEvent(List<Long> postIds){
        PostEvent event = PostEvent.builder()
                .eventType("DELETE_BATCH")
                .postIds(postIds)
                .build();
        kafkaTemplate.send(POST_EVENT_TOPIC, event);
    }

}
