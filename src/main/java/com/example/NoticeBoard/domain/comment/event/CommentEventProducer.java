package com.example.NoticeBoard.domain.comment.event;

import com.example.NoticeBoard.domain.comment.entity.CommentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

// kafka 이벤트 발행
@Service
@RequiredArgsConstructor
public class CommentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String COMMENT_EVENT_TOPIC = "comment-event-topic";

    public void sendCommentCreatedEvent(Long commentId, Long postId) {
        CommentEvent event = CommentEvent.builder()
                .eventType("CREATE")
                .commentId(commentId)
                .postId(postId)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
    }

    public void sendCommentUpdateEvent(Long commentId, Long postId) {
        CommentEvent event = CommentEvent.builder()
                .eventType("UPDATE")
                .commentId(commentId)
                .postId(postId)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
    }

    public void sendCommentDeleteEvent(Long commentId, Long postId) {
        CommentEvent event = CommentEvent.builder()
                .eventType("DELETE")
                .commentId(commentId)
                .postId(postId)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
    }

    public void sendCommentDeleteBatchEvent(List<Long> commentIds){
        CommentEvent event = CommentEvent.builder()
                .eventType("DELETE_BATCH")
                .commentIds(commentIds)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
    }
}
