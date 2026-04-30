package com.example.NoticeBoard.domain.comment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

// kafka 이벤트 발행
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String COMMENT_EVENT_TOPIC = "comment-event-topic";

    public void sendCommentCreatedEvent(Long commentId, Long postId) {
        CommentEvent event = CommentEvent.builder()
                .eventType(CommentEventType.CREATE)
                .commentId(commentId)
                .postId(postId)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
        log.info("댓글 이벤트 발행: type={}, commentId={}, postId={}", event.getEventType(), commentId, postId);
    }

    public void sendCommentUpdateEvent(Long commentId, Long postId) {
        CommentEvent event = CommentEvent.builder()
                .eventType(CommentEventType.UPDATE)
                .commentId(commentId)
                .postId(postId)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
        log.info("댓글 이벤트 발행: type={}, commentId={}, postId={}", event.getEventType(), commentId, postId);
    }

    public void sendCommentDeleteEvent(Long commentId, Long postId) {
        CommentEvent event = CommentEvent.builder()
                .eventType(CommentEventType.DELETE)
                .commentId(commentId)
                .postId(postId)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
        log.info("댓글 이벤트 발행: type={}, commentId={}, postId={}", event.getEventType(), commentId, postId);
    }

    public void sendCommentDeleteBatchEvent(List<Long> commentIds){
        CommentEvent event = CommentEvent.builder()
                .eventType(CommentEventType.DELETE_BATCH)
                .commentIds(commentIds)
                .build();
        kafkaTemplate.send(COMMENT_EVENT_TOPIC, event);
    }
}
