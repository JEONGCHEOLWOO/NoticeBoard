package com.example.NoticeBoard.domain.comment.dto;

import com.example.NoticeBoard.global.enumeration.CommentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CommentRequestDto {

    @NotBlank
    private String content;     // 댓글 내용

    private String imageUrl;    // 이미지 주소

    private String gifUrl;      // gif 주소

    private CommentStatus commentStatus; // 댓글 종류

    private Long parentId;      // 부모 댓글 아이디
}
