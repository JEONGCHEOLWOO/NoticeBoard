package com.example.NoticeBoard.dto;

import com.example.NoticeBoard.entity.Post;
import com.example.NoticeBoard.enumeration.Category;
import com.example.NoticeBoard.enumeration.PostStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PostResponseDto {

    private Long id; // 내부 PK (식별자)

    private Category category; // 카테고리
 
    private String title; // 게시글 제목

    private String content; // 게시글 내용

    private String nickname; // 게시글 작성자 닉네임

    private int viewCount; // 조회수

    private int likeCount; // 게시글 좋아요 수

    private PostStatus postStatus; // 게시글 종류(일반 게시글, 비밀 게시글, 삭제된 게시글, 블라인드된 게시글 등)

    private LocalDateTime createdAt; // 게시글 작성 시간

    private  LocalDateTime updatedAt; // 게시글 수정 시간

    public static PostResponseDto fromEntity(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .nickname(post.getUser().getNickname())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .postStatus(post.getPostStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
