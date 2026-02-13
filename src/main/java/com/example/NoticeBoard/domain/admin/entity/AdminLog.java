package com.example.NoticeBoard.domain.admin.entity;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.global.enumeration.ActionType;
import com.example.NoticeBoard.global.enumeration.ReportReason;
import com.example.NoticeBoard.global.enumeration.ReportType;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "admin_log")
// 관리자가 무엇을 처리했는가
public class AdminLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin; // 관리자 권한을 가진 user

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActionType actionType; // 신고 처리 유형 Enum: BLIND_POST, UNBLIND_POST, DELETE_POST, RESTORE_POST, BLIND_COMMENT, UNBLIND_COMMENT, DELETE_COMMENT, RESTORE_COMMENT, BAN_USER, UNBAN_USER

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 신고 당한 유저 Id (게시글 작성자 or 댓글 작성자)

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post; // 신고 당한 게시글 Id

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment; // 신고 당한 댓글 Id

    @Enumerated(EnumType.STRING)
    private ReportType reportType; // 신고 유형 Enum: POST, COMMENT, USER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportReason reportReason; // 자주 쓰이는 신고 사유 Enum : SPAM, INAPPROPRIATE, HATE_SPEECH, VIOLENCE, COPYRIGHT, PERSONAL_INFO, OTHER

    @Column(columnDefinition = "TEXT")
    private String detail; // 신고 내용(상세 내용)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 로그 생성 시간
}
