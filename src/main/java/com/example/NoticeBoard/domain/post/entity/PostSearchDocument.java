package com.example.NoticeBoard.domain.post.entity;

import com.example.NoticeBoard.global.enumeration.Category;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "posts")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@Builder
// ES(Elasticsearch) 검색 전용 저장 모델 Document - 검색에 사용될 데이터(제목, 작성자, 내용 등) + 검색 결과 화면에 바로 보여줄 데이터 가 있어야됌.
public class PostSearchDocument {

    @Id
    private String id; // Elasticsearch의 Id

    @Field(type = FieldType.Long)
    private Long postId; // 게시글 Id

    @Field(type = FieldType.Long)
    private Long userId; // 작성자 Id

    @Field(type = FieldType.Keyword)
    private Category category; 

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    @Field(type = FieldType.Text)
    private String nickname;

    @Field(type = FieldType.Boolean)
    private boolean image; // 이미지 유무

    @Field(type = FieldType.Keyword)
    private PostStatus postStatus;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount; // 댓글수

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;
}
