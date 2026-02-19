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
// ES(Elasticsearch) 검색 전용 Document
public class PostSearchDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long)
    private Long postId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private Category category;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    @Field(type = FieldType.Text)
    private String nickname;

    @Field(type = FieldType.Keyword)
    private PostStatus postStatus;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    public static PostSearchDocument fromEntity(PostSearchDocument postSearchDocument){
        return PostSearchDocument.builder()
                .id(postSearchDocument.id)
                .title(postSearchDocument.getTitle())
                .content(postSearchDocument.getContent())
                .postId(postSearchDocument.getPostId())
                .userId(postSearchDocument.getUserId())
                .category(postSearchDocument.getCategory())
                .postStatus(postSearchDocument.getpostStatus())
                .nickname(postSearchDocument.getNickname())
                .createdAt(postSearchDocument.getCreatedAt())
                .updatedAt(postSearchDocument.getUpdatedAt())
                .build();
    }
}
