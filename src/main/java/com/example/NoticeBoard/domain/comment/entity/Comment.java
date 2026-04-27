package com.example.NoticeBoard.domain.comment.entity;

import com.example.NoticeBoard.global.enumeration.CommentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

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
    @Column(name = "image_url")
    private String imageUrl; // 이미지 주소

    @Column(name = "gif_url")
    private String gifUrl; // GIF 주소

    @Column(name = "user_id", nullable = false)
    private Long userId; // 댓글 작성자 id

    @Column(name = "post_id", nullable = false)
    private Long postId; // 게시글 id

    @Column(name = "parent_id")
    private Long parentId; // 대댓글의 부모 Id (null 이면 최상위 댓글)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CommentStatus commentStatus; // 댓글 종류 (일반 댓글, 비밀 댓글, 삭제된 댓글, 블라인드된 댓글 등)

    @Column(nullable = false)
    private Long likeCount; // 좋아요 수 (캐시)

    @CreationTimestamp
    @Column (nullable = false, updatable = false)
    private LocalDateTime createdAt; // 댓글 작성 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 댓글 수정 날짜

    private LocalDateTime deletedAt; // 댓글 삭제 요청 날짜

    // ------------------비즈니스 메소드-------------------
    // 댓글 수정
    public boolean isChanged(String content, String imageUrl, String gifUrl, CommentStatus commentStatus){
        boolean changed = false;

        if(!Objects.equals(this.content, content)){
            this.content = content;
            changed = true;
        }

        if(!Objects.equals(this.imageUrl, imageUrl)){
            this.imageUrl = imageUrl;
            changed = true;
        }

        if(!Objects.equals(this.gifUrl, gifUrl)){
            this.gifUrl = gifUrl;
            changed = true;
        }

        if(!Objects.equals(this.commentStatus, commentStatus)){
            this.commentStatus = commentStatus;
            changed = true;
        }

        return changed;
    }

    // 댓글 삭제 (Soft Delete)
    public void delete(){
        this.commentStatus = CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제된 댓글인지 확인
    public boolean isDeleted() {
        return this.commentStatus == CommentStatus.DELETED || this.deletedAt != null;
    }
}
