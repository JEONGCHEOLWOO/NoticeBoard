package com.example.NoticeBoard.service;

import com.example.NoticeBoard.dto.CommentReportRequestDto;
import com.example.NoticeBoard.dto.CommentRequestDto;
import com.example.NoticeBoard.dto.CommentResponseDto;
import com.example.NoticeBoard.entity.*;
import com.example.NoticeBoard.enumeration.CommentStatus;
import com.example.NoticeBoard.enumeration.PostStatus;
import com.example.NoticeBoard.enumeration.ReportStatus;
import com.example.NoticeBoard.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentReportRepository commentReportRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 댓글 생성
    public CommentResponseDto createComment(Long postId, Long userId, CommentRequestDto commentRequestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        // 연속 댓글 제한 (60초) - 도배 위험
        List<Comment> recent = commentRepository.findTop2ByPostIdAndUserIdOrderByCreatedAtDesc(postId, userId);

        if (!recent.isEmpty()) {
            LocalDateTime last = recent.get(0).getCreatedAt();
            if (Duration.between(last, LocalDateTime.now()).getSeconds() < 60){
                throw new IllegalStateException("댓글은 60초에 1개만 작성할 수 있습니다.");
            }
        }

        // 댓글 1개당 이미지 1개
        if (commentRequestDto.getImageUri() != null && commentRequestDto.getImageUri().contains(",")) {
            throw new IllegalArgumentException("댓글에는 이미지를 1개만 첨부할 수 있습니다.");
        }

        // GIF 제한 (한 사람당 게시글에 GIF 최대 3개, 그 이상 업로드 시 가장 오래된 GIF 댓글 삭제) - 도배 위험
        if (commentRequestDto.isGif()) {
            long gifCount = commentRepository.countByPostIdAndUserIdAndGifTrue(postId, userId);
            if (gifCount >= 3) {
                // 가장 오래된 GIF 댓글 삭제
                List<Comment> oldestGif = commentRepository
                        .findByPostIdOrderByCreatedAtAsc(postId)
                        .stream()
                        .filter(Comment::isGif)
                        .toList();

                if (!oldestGif.isEmpty()) {
                    commentRepository.delete(oldestGif.get(0));
                }
            }
        }

        // 대댓글의 부모 댓글
        Comment parent = null;
        if (commentRequestDto.getParentId() != null) {
            parent = commentRepository.findById(commentRequestDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 없습니다."));
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .parent(parent)
                .content(commentRequestDto.getContent())
                .imageUri(commentRequestDto.getImageUri())
                .gif(commentRequestDto.isGif())
                .fileUri(commentRequestDto.getFileUri())
                .commentStatus(CommentStatus.NORMAL)
                .likeCount(0)
                .build();

        return CommentResponseDto.fromEntity(commentRepository.save(comment));
    }

    // 댓글 수정 - IllegalArgumentException 와 IllegalStateException 차이
    public CommentResponseDto updateComment(Long commentId, Long userId, CommentRequestDto requestDto){
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.setContent(requestDto.getContent());
        if (requestDto.getImageUri() != null) {
            comment.setImageUri(requestDto.getImageUri());
        }

        if (requestDto.getFileUri() != null) {
            comment.setFileUri(requestDto.getFileUri());
        }
        return CommentResponseDto.fromEntity(comment);

    }

    // 댓글 삭제
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        comment.setDeletedAt(LocalDateTime.now());
        comment.setCommentStatus(CommentStatus.DELETED);
    }

    // 댓글 좋아요
    public void likeComment(Long commentId, Long userId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        CommentLike commentLike = CommentLike.builder()
                .comment(comment)
                .user(user)
                .build();

        commentLikeRepository.save(commentLike);

        // 중복 좋아요 체크는 CommentLike 테이블 기준
        comment.setLikeCount(comment.getLikeCount() + 1);
    }

    // 댓글 좋아요 취소
    public void unlikeComment(Long commentId, Long userId) {

        CommentLike commentLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new IllegalStateException("좋아요 기록이 없습니다."));


        commentLikeRepository.delete(commentLike);

        Comment comment = commentLike.getComment();
        comment.setLikeCount(comment.getLikeCount() - 1);
    }

    // 댓글 신고
    public void reportComment(Long commentId, Long userId, CommentReportRequestDto requestDto) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        if (commentReportRepository.existsByCommentIdAndUserId(commentId, userId)) {
            throw new IllegalStateException("이미 신고한 댓글입니다.");
        }

        if(comment.getUser().getId().equals(userId)){
            throw new IllegalArgumentException("본인이 작성한 댓글은 신고할 수 없습니다.");
        }

        CommentReport report = CommentReport.builder()
                .comment(comment)
                .user(user)
                .reportReason(requestDto.getReason())
                .content(requestDto.getText())
                .reportStatus(ReportStatus.PROCESSING)
                .build();

        commentReportRepository.save(report);

        // 신고 5회 이상 → 자동 블라인드
        if (comment.getReports().size() >= 5) {
            comment.setCommentStatus(CommentStatus.BLIND);
        }
    }
}
