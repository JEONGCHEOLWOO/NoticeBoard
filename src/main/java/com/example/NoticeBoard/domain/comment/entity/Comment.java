package com.example.NoticeBoard.domain.comment.entity;

import com.example.NoticeBoard.global.enumeration.CommentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 댓글 내용

    // 하나의 댓글엔 하나의 이미지만
    @Column(name = "image_uri")
    private String imageUri; // 이미지 주소

    @Column(name = "file_uri")
    private String fileUri; // 파일 주소

    @Column(nullable = false)
    private boolean gif = false; // GIF 여부

    @Column(name = "user_id", nullable = false)
    private Long user; // 댓글 작성자 id

    @Column(name = "post_id", nullable = false)
    private Long post; // 게시글 id

    @Column(name = "parent_id")
    private Long parentId; // 대댓글의 부모 Id (null 이면 최상위 댓글)

    @Column(nullable = false)
    private Integer likeCount = 0; // 좋아요 수 (캐시)

    @CreationTimestamp
    @Column (nullable = false, updatable = false)
    private LocalDateTime createdAt; // 댓글 작성 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 댓글 수정 날짜

    private LocalDateTime deletedAt; // 댓글 삭제 요청 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CommentStatus commentStatus; // 댓글 종류 (일반 댓글, 비밀 댓글, 삭제된 댓글, 블라인드된 댓글 등)
}
