package com.example.NoticeBoard.domain.comment.event;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.entity.CommentEvent;
import com.example.NoticeBoard.domain.comment.entity.CommentSearchDocument;
import com.example.NoticeBoard.domain.comment.repository.CommentRepository;
import com.example.NoticeBoard.domain.comment.repository.CommentSearchRepository;
import com.example.NoticeBoard.global.enumeration.CommentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentEventConsumer {

    private final CommentRepository commentRepository;
    private final CommentSearchRepository commentSearchRepository;

    @KafkaListener(topics = "commnet-event-topic", groupId = "commnet-group", concurrency = "3")
    public void consume(CommentEvent event){

        log.info("댓글 CUD Kafka 이벤트 수신 type={}", event.getEventType());

        switch (event.getEventType()){
            case "CREATE":
            case "UPDATE":
                indexComment(event.getCommentId());
                break;
            case "DELETE":
                deleteComment(event.getCommentId());
                break;
            case "DELETE_BATCH":
                deleteCommentBulk(event.getCommentIds());
                break;
        }

    }

    private void indexComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new RuntimeException("댓글이 존재하지 않습니다."));

        CommentSearchDocument document = CommentSearchDocument.from(comment);
        commentSearchRepository.save(document);
        log.info("Elasticsearch 저장/업데이트 완료 commentId={}", commentId);
    }

    private void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new RuntimeException("댓글이 존재하지 않습니다."));

        if (comment.getCommentStatus() == CommentStatus.DELETED){
            commentSearchRepository.deleteById(commentId);
            log.info("Elasticsearch 삭제 완료 commentId={}", commentId);
        }
    }

    // Scheduler가 실행되면(새벽 3시로 설정) 30일이 지난 DELETED 데이터를 조회하면 여러 개의 데이터가 나오는데
    // DB에서 이걸 전부 Hard DELETE을 하고 Kafka 로 DELETE_BATCH 이벤트를 생성해서
    // Topic에 전달하고 Consumer에서 처리할 때 호출
    private void deleteCommentBulk(List<Long> commentIds) {
        commentSearchRepository.deleteAllById(commentIds);
        log.info("Elasticsearch bulk 삭제 완료 count={}", commentIds.size());
    }
}

