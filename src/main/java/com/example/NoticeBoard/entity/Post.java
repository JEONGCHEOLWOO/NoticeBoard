package com.example.NoticeBoard.entity;

import com.example.NoticeBoard.enumeration.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // 내부 PK
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    // Enum: FREE, NOTICE, QNA
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    // 게시글 작성자 id (User PK)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String title; // 게시글 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 게시글 내용

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    // 게시글 댓글 목록, 게시글 하나에 여러개의 댓글 존재. 1:N 관계
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    // 게시글 좋아요 목록, 게시글 하나에 여러개의 좋아요 존재. 1:N 관계
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    // 게시글에 대한 신고 목록, 게시글 하나에 여러개의 신고 존재. 1:N 관계
    private List<PostReport> reports = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    // 게시글에 파일 업로드, 게시글 하나에 여러개의 파일 존재, 1:N 관계
    private List<FileUpload> attachments = new ArrayList<>();

    private int viewCount = 0; // 조회수

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 게시글 등록 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 게시글 업데이트 날짜

    private int likeCount; // 좋아요 수 -> 캐시 용도
}
