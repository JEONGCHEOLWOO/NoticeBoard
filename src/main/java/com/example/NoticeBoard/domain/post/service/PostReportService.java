package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.report.dto.PostReportRequestDto;
import com.example.NoticeBoard.domain.report.entity.PostReport;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import com.example.NoticeBoard.global.enumeration.ReportStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostReportService {


    // 게시글 신고
    public void reportPost(Long postId, Long userId, PostReportRequestDto requestDto) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        if (postReportRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new IllegalStateException("이미 신고한 게시글입니다.");
        }

        if (post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글은 신고할 수 없습니다.");
        }

        PostReport report = PostReport.builder()
                .post(post)
                .user(user)
                .content(requestDto.getContent())
                .reportReason(requestDto.getReason())
                .reportStatus(ReportStatus.PROCESSING)
                .build();

        postReportRepository.save(report);

        // 신고 5회 이상 → 자동 블라인드
        if (post.getReports().size() >= 5) {
            post.setPostStatus(PostStatus.BLIND);
        }
    }

}
