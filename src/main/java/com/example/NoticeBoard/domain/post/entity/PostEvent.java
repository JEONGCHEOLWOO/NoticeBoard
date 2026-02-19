package com.example.NoticeBoard.domain.post.entity;

import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor @NoArgsConstructor
// Post의 CUD(Create, Update, Delete) 작업 시 Elasticsearch와 동기화 하기 위한 이벤트 객체
public class PostEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long postId; // 게시글 Id
    private String eventType; // 이벤트 타입: CREATE, UPDATE, DELETE
    private PostResponseDto postResponseDto; // 게시글 데이터 (DELETE인 경우 null)
    private LocalDateTime eventTime; // 이벤트 발행 시간
    private String eventId; // 이벤트 Id (중복 처리 방지용)

    public PostEvent(Long postId, String eventType, PostResponseDto postResponseDto){
        this.postId = postId;
        this.eventType = eventType;
        this.postResponseDto = postResponseDto;
        this.eventTime = LocalDateTime.now();
        this.eventId = postId + "-" + eventType + "-" + System.currentTimeMillis();
    }
}
