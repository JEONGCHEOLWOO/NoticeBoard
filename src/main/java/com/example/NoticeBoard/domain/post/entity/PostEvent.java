package com.example.NoticeBoard.domain.post.entity;

import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor @NoArgsConstructor
// Post의 CUD(Create, Update, Delete) 작업 시 Elasticsearch와 동기화 하기 위한 이벤트 객체 - 각자 event를 분리할 필요가 있음. 필드가 같아도 분리를 하면 나중에 수정이 용이하고, 불필요한 데이터의 낭비가 없어짐.
public class PostEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long postId; // 게시글 Id
    private String eventType; // 이벤트 타입: CREATE, UPDATE, DELETE
    private LocalDateTime eventTime; // 이벤트 발행 시간
    private String eventId; // 이벤트 Id (중복 처리 방지용)

    public PostEvent(Long postId, String eventType){
        this.postId = postId;
        this.eventType = eventType;
        this.eventTime = LocalDateTime.now();
        this.eventId = postId + "-" + eventType + "-" + System.currentTimeMillis();
    }
}
