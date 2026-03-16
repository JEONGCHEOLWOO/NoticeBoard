package com.example.NoticeBoard.domain.post.event;

import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.post.repository.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final PostRepository postRepository;
    private final PostSearchRepository postSearchRepository;
    
    @KafkaListener(topics = "post-event-topic", groupId = "post-group")
    public void consume(ConsumerRecord<String, Long> record){
        String eventType = record.key();
        Long postId = record.value();
        
        log.info("kafka 이벤트 수신 type={}, postId={}", eventType, postId);
        
        switch (eventType){
            case "CREATE":
            case "UPDATE":
                indexPost(postId);
                break;
            case "DELETE":
                deletePost(postId);
                break;
        }
    }

    private void indexPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("게시글이 존재하지 않습니다."));

        PostSearchDocument document = PostSearchDocument.from(post);
        postSearchRepository.save(document);
        log.info("Elasticsearch 저장/업데이트 완료 postId={}", postId);
    }

    private void deletePost(Long postId) {
        postSearchRepository.deleteById(postId);
        log.info("Elasticsearch 삭제 완료 postId={}", postId);
    }


}
