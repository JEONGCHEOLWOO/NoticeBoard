package com.example.NoticeBoard.global.common.validator;

import com.example.NoticeBoard.global.enumeration.UploadContext;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageValidator {

    // 이미지 정책 검사
    public static void validate(MultipartFile file, UploadContext context) {

        String type = file.getContentType();
        long size = file.getSize();

        if (!type.startsWith("image/")) {
            throw new IllegalArgumentException("이미지만 업로드 가능합니다");
        }

        // GIF 용량 제한 (게시글, 댓글)
        if ("image/gif".equals(type) && size > 2_000_000) {
            throw new IllegalArgumentException("GIF는 2MB 이하만 업로드 가능합니다");
        }

        // 댓글 이미지 용량
        if (context == UploadContext.COMMENT && size > 1_000_000) {
            throw new IllegalArgumentException("댓글 이미지는 1MB 이하만 허용됩니다");
        }
    }
}
