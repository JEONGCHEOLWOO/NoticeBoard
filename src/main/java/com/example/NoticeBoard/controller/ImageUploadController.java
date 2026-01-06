package com.example.NoticeBoard.controller;

import com.example.NoticeBoard.enumeration.UploadContext;
import com.example.NoticeBoard.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageUploadController {

    private final ImageService imageService;

    // 게시글 이미지 업로드
    @PostMapping("/post")
    public ResponseEntity<Map<String, String>> uploadPostImage(@RequestParam("image")MultipartFile image){
        String imageUri = imageService.upload(image, UploadContext.POST);
        return ResponseEntity.ok(Map.of("imageUri", imageUri));
    }

    // 댓글 이미지 업로드
    @PostMapping("/comment")
    public ResponseEntity<Map<String, String>> uploadCommentImage(@RequestPart("image") MultipartFile image) {
        String imageUri = imageService.upload(image, UploadContext.COMMENT);
        return ResponseEntity.ok(Map.of("imageUri", imageUri));
    }

}
