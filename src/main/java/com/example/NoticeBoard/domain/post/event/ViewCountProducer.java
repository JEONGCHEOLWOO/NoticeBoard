package com.example.NoticeBoard.domain.post.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// kafka 이벤트 발행
@Service
@RequiredArgsConstructor
public class ViewCountProducer {

    public final KafkaTemplate<String, Object> kafkaTemplate;

    public static final String POST_VIEW_TOPIC = "post-view-topic";

    // PostCommandService에서 '게시글 조회 발생' 이벤트를 kafka에 보내면 Topic에 메세지를 발행 및 저장 
    public void sendViewEvent(Long postId){
        kafkaTemplate.send(POST_VIEW_TOPIC, postId);
    }
}
