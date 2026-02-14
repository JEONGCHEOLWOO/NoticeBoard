package com.example.NoticeBoard.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "post_like",
       uniqueConstraints = @UniqueConstraint(name = "uk_post_user",
       columnNames = {"post_id", "user_id"}))
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @JoinColumn(name = "post_id", nullable = false)
    private Long postId; // 게시글 id

    @JoinColumn(name = "user_id", nullable = false)
    private Long userId; // 좋아요를 누른 사람의 id

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 좋아요를 누른 시간 -> 추후 "최근 좋아요가 많은 게시글" 같은 기준을 만들때 필요. 현재는 필요X
}
