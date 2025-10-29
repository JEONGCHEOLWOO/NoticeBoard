package com.example.NoticeBoard.entity;

import com.example.NoticeBoard.enumeration.CommentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 댓글 내용

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // FK -> 댓글 작성자 id

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // FK -> 게시글 id

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent; // 대댓글의 부모 (null 이면 최상위 댓글)

    @OneToMany (mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>(); // 대댓글 목록

    @OneToMany (mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentLike> likes = new ArrayList<>(); // 댓글 좋아요 목록

    @OneToMany (mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentReport> reports = new ArrayList<>(); // 댓글 신고 목록

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 댓글 작성 시간

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 댓글 수정 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CommentStatus commentStatus; // 댓글 종류 (일반 댓글, 비밀 댓글, 삭제된 댓글 등)

    private int likeCount; // 좋아요 수
}
