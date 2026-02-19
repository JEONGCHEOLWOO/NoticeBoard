package com.example.NoticeBoard.domain.post.dto;

import com.example.NoticeBoard.global.enumeration.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PostRequestDto {
    @NotNull(message = "카테고리는 필수 입니다.")
    private Category category;  // 게시글 카테고리
    
    @NotBlank(message = "제목은 필수 입니다.")
    @Size(min = 1, max = 100)
    private String title;       // 게시글 제목

    @NotBlank(message = "내용은 필수 입니다.")
    @Size(min = 1)
    private String content;     // 게시글 내용
    
    private String imageUri;    // 이미지 주소
    
    private String fileUri;     // 파일 주소
}
