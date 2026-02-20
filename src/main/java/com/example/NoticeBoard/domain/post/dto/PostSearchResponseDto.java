package com.example.NoticeBoard.domain.post.dto;

import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.global.enumeration.Category;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
// 게시글 검색 및 초기 페이지 응답 Dto
public class PostSearchResponseDto {
    private String id; // 내부 PK (식별자)
    private Category category; // 카테고리
    private String title; // 게시글 제목
    private Long postId; // 게시글 Id
    private Long userId; // 게시글 작성자 - User 정보 조회용
    private String nickname; // 게시글 작성자 닉네임
    private Boolean image; // 게시글에 이미지 존재 여부(리스트에 표시하기 위함)
    private Integer viewCount; // 조회수
    private Integer likeCount; // 게시글 좋아요 수
    private Integer commentCount; // 댓글수
    private PostStatus postStatus; // 게시글 종류(일반 게시글, 비밀 게시글, 삭제된 게시글, 블라인드된 게시글 등)
    private LocalDateTime createdAt; // 게시글 작성 시간

    public static PostSearchResponseDto fromEntity(PostSearchDocument document) {
        return PostSearchResponseDto.builder()
                .id(document.getId()) // Elasticsearch Id
                .category(document.getCategory())
                .title(document.getTitle())
                .userId(document.getUserId())
                .postId(document.getPostId())
                .nickname(document.getNickname())
                .image(document.isImage())
                .viewCount(document.getViewCount())
                .likeCount(document.getLikeCount())
                .commentCount(document.getCommentCount)
                .postStatus(document.getPostStatus())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
