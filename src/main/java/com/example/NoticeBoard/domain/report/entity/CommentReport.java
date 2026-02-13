package com.example.NoticeBoard.domain.report.entity;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.global.enumeration.ReportReason;
import com.example.NoticeBoard.global.enumeration.ReportStatus;
import com.example.NoticeBoard.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "comment_report")
public class CommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment; // 신고 당한 댓글 id (Comment PK)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 신고한 사람의 id (User PK)

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false, updatable = false)
    private String content; // 신고 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportReason reportReason; // 자주 쓰이는 신고 사유 Enum : SPAM, INSULT, HARASSMENT, SEXUAL_CONTENT, OTHER

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 신고 접수 시간

    private LocalDateTime reviewedAt; // 검토 시작 시간(들어오자 마자 바로 PROCESSING으로 설정)
    private LocalDateTime resolvedAt; // 처리 완료 시간(상태가 RESOLVED으로 바뀔 때 설정)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReportStatus reportStatus; // 신고 상태 Enum : PROCESSING, RESOLVED
}
