package com.example.NoticeBoard.domain.comment.entity;


import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "comments")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
// ES(Elasticsearch) 검색 전용 저장 모델 Document - 검색에 사용될 데이터(제목, 작성자, 내용 등) + 검색 결과 화면에 바로 보여줄 데이터 가 있어야됌.
public class CommentSearchDocument {
    public static CommentSearchDocument from(Comment comment) {
    }
}
