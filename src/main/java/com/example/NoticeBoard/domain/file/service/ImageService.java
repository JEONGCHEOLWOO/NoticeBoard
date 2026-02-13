package com.example.NoticeBoard.domain.file.service;

import com.example.NoticeBoard.global.common.validator.ImageValidator;
import com.example.NoticeBoard.global.enumeration.UploadContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageValidator imageValidator;

    // 이미지 업로드 (게시글, 댓글)
    public String upload(MultipartFile file, UploadContext context){

        // 이미지 정책 검사
        imageValidator.validate(file, context);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get("uploads", fileName);

        try {
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
        }catch (IOException e){
            throw new RuntimeException("이미지 업로드 실패", e);
        }

        return "/uploads/" + fileName;
    }

 }
