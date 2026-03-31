package com.example.NoticeBoard.domain.comment.dto;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.global.enumeration.CommentStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CommentResponseDto {

    private Long id; // 내부 PK

    private String content;     // 댓글 내용

    private String imageUrl;    // 이미지 주소

    private String gifUrl;      // gif 주소

    private Long userId;        // FK -> 댓글 작성자 id

//    private String nickname;    // 대댓글 작성자 부모의 닉네임

    private Long postId;        // FK -> 게시글 id

    private Long parentId;      // 대댓글 부모의 id

    private Long likeCount;      // 좋아요 수

    private CommentStatus commentStatus; // 댓글 종류(일반 댓글, 비밀 댓글, 삭제된 댓글 등)

    private LocalDateTime createdAt; // 댓글 작성 시간

    private LocalDateTime updatedAt; // 댓글 수정 시간

//    private List<CommentResponseDto> replies; // 대댓글

    public static CommentResponseDto fromEntity(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .imageUrl(comment.getImageUrl())
                .gifUrl(comment.getGifUrl())
                .userId(comment.getUserId())
                .postId(comment.getPostId())
                .parentId(
                        comment.getParentId() != null
                                ? comment.getParentId()
                                : null
                )
                .likeCount(comment.getLikeCount())
                .commentStatus(comment.getCommentStatus())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
//                .replies(
//                        comment.getReplies() == null
//                                ? List.of()
//                                : comment.getReplies()
//                                .stream()
//                                .map(CommentResponseDto::fromEntity)
//                                .collect(Collectors.toList())
//                )
                .build();
    }
}
