package com.example.NoticeBoard.domain.post.event;

import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.entity.PostEvent;
import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.post.repository.PostSearchRepository;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostEventConsumer {

    private final PostRepository postRepository;
    private final PostSearchRepository postSearchRepository;
    
    @KafkaListener(topics = "post-event-topic", groupId = "post-group", concurrency = "3")
    public void consume(PostEvent event){

        log.info("게시글 CUD kafka 이벤트 수신 type={}", event.getEventType());
        
        switch (event.getEventType()){
            case "CREATE":
            case "UPDATE":
                indexPost(event.getPostId());
                break;
            case "DELETE":
                deletePost(event.getPostId());
                break;
            case "DELETE_BATCH":
                deletePostBulk(event.getPostIds());
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
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("게시글이 존재하지 않습니다."));

        if (post.getPostStatus() == PostStatus.DELETED){
            postSearchRepository.deleteById(postId);
            log.info("Elasticsearch 삭제 완료 postId={}", postId);
        }
    }

    // Scheduler가 실행되면(새벽 3시로 설정) 30일이 지난 DELETED 데이터를 조회하면 여러 개의 데이터가 나오는데
    // DB에서 이걸 전부 Hard DELETE을 하고 Kafka 로 DELETE_BATCH 이벤트를 생성해서
    // Topic에 전달하고 Consumer에서 처리할 때 호출
    private void deletePostBulk(List<Long> postIds) {
        postSearchRepository.deleteAllById(postIds);
        log.info("Elasticsearch bulk 삭제 완료 count={}", postIds.size());
    }
}
