package com.example.NoticeBoard.domain.comment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "comment_like",
       uniqueConstraints = @UniqueConstraint(name = "uk_comment_user",
       columnNames = {"comment_id", "user_id"}))
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @JoinColumn(name = "comment_id", nullable = false)
    private Long commentId; // 댓글 id

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId; // 좋아요를 누른 사람의 id

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 좋아요를 가장 많이 받은 댓글 조회시 필요. 현재는 X
}
