package com.example.NoticeBoard.dto;

import com.example.NoticeBoard.enumeration.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PostRequestDto {
    @NotBlank
    private Category category;  // 게시글 카테고리
    
    @NotBlank
    private String title;       // 게시글 제목

    @NotBlank
    private String content;     // 게시글 내용
    
    private String imageUri;    // 이미지 주소
    
    private String fileUri;     // 파일 주소
}
