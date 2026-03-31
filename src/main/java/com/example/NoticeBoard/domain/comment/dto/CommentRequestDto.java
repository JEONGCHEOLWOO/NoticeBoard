package com.example.NoticeBoard.domain.comment.dto;

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

    private Long parentId;      // 부모 댓글 아이디
}
