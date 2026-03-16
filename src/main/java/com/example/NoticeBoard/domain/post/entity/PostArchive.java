package com.example.NoticeBoard.domain.post.entity;

import com.example.NoticeBoard.global.enumeration.Category;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "post_archive")
public class PostArchive {

    @Id
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private Category category;

    private String nickname;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public static PostArchive from(Post post){
        return PostArchive.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .title(post.getTitle())
                .content(post.getContent())
                .category(post.getCategory())
                .nickname(post.getNickname())
                .createdAt(post.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
