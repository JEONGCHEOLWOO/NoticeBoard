package com.example.NoticeBoard.domain.admin.dto;

import com.example.NoticeBoard.domain.admin.entity.AdminLog;
import com.example.NoticeBoard.global.enumeration.ActionType;
import com.example.NoticeBoard.global.enumeration.ReportReason;
import com.example.NoticeBoard.global.enumeration.ReportType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AdminResponseDto {

    private Long logId; // 로그 아이디

    private String adminEmail; // 관리자 이메일

    private ActionType actionType; // 신고 처리 유형 Enum: BLOCK_POST, DELETE_POST, BLOCK_COMMENT, DELETE_COMMENT, BAN_USER

    private ReportType reportType; // 신고 유형 Enum: POST, COMMENT

    private ReportReason reportReason; // 신고 사유 Enum : SPAM, INAPPROPRIATE, HATE_SPEECH, VIOLENCE, COPYRIGHT, PERSONAL_INFO, OTHER

    private Long userId; // 신고 당한 유저 ID

    private Long postId; // 신고 당한 게시글 ID

    private Long commentId; // 신고 당한 댓글 ID

    private String detail; // 신고 내용(상세 내용)

    private LocalDateTime createdAt; // 로그 생성 시간

    public static AdminResponseDto fromEntity(AdminLog admin) {
        return AdminResponseDto.builder()
                .logId(admin.getId())
                .adminEmail(admin.getAdmin().getEmail())
                .actionType(admin.getActionType())
                .reportType(admin.getReportType())
                .reportReason(admin.getReportReason())
                .userId(admin.getUser().getId())
                .postId(admin.getPost().getId() != null ? admin.getPost().getId() : null)
                .commentId(admin.getComment().getId() != null ? admin.getComment().getId() : null)
                .detail(admin.getDetail())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}
