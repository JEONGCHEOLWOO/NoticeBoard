package com.example.NoticeBoard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "comment_like",
       uniqueConstraints = @UniqueConstraint(name = "uk_comment_like_comment_user",
       columnNames = {"comment_id", "user_id"}))
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment; // FK -> 댓글 id (댓글 PK)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // FK -> 좋아요를 누른 사람의 id (User PK)

}
