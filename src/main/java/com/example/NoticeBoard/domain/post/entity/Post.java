package com.example.NoticeBoard.domain.post.entity;

import com.example.NoticeBoard.global.enumeration.Category;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 내부 PK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Category category;  // Enum: FREE, NOTICE, QNA

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 게시글 작성자 id

    @Column(nullable = false, length = 100)
    private String title; // 게시글 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 게시글 내용

    @Column(name = "image_uri")
    private String imageUri; // 이미지 주소

    @Column(name = "file_uri")
    private String fileUri; // 파일 주소

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 10)
    private PostStatus postStatus; // 게시글 종류 (일반 게시글, 비밀 게시글, 삭제된 게시글, 블라인드된 게시글 등)

    @Column(nullable = false)
    private Integer viewCount = 0; // 조회수

    @Column(nullable = false)
    private Integer likeCount = 0; // 좋아요 수 -> 캐시 용도

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 게시글 등록 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 게시글 업데이트 날짜
    
    private LocalDateTime deletedAt; // 게시글 삭제 요청 날짜

    // ------------------비즈니스 메소드-------------------
    // 게시글 수정
    public void update(String title, String content, Category category) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
        if (category != null) {
            this.category = category;
        }
    }

    // 게시글 삭제 (Soft Delete)
    public void delete(){
        this.postStatus = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제된 게시글인지 확인
    public boolean isDeleted() {
        return this.postStatus == PostStatus.DELETED || this.deletedAt != null;
    }

    public void incrementViewCount() {
    }
}
