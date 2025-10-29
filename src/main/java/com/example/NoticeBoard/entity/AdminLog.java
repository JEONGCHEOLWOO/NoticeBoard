package com.example.NoticeBoard.entity;

import com.example.NoticeBoard.enumeration.ActionType;
import com.example.NoticeBoard.enumeration.ReportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "admin_log")
public class AdminLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin; // 관리자 권한을 가진 user

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActionType actionType; // 신고 처리 유형 Enum: BLOCK_POST, DELETE_COMMENT, BAN_USER

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 신고 당한 대상자 id (게시글 작성자 or 댓글 작성자)

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post; // 신고 당한 대상자가 작성한 신고된 게시글 id

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment; // 신고 당한 대상자가 작성한 신고된 댓글 id

    @Enumerated(EnumType.STRING)
    private ReportType reportType; // 신고 유형 Enum: POST, COMMENT

    @Column(columnDefinition = "TEXT")
    private String detail; // 신고 내용(상세 내용)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 로그 생성 시간
}
