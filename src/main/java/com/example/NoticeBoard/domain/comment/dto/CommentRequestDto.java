package com.example.NoticeBoard.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CommentRequestDto {

    @NotBlank
    private String content;     // 댓글 내용

    private String imageUri;    // 이미지 주소

    private String fileUri;     // 파일 주소

    private boolean gif;        // gif 유무

    private Long parentId;      // 부모 댓글 아이디
}
