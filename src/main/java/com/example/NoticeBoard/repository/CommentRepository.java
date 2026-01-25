package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 조회
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 특정 게시글에서 사용자의 GIF 개수
    long countByPostIdAndUserIdAndGifTrue(Long postId, Long userId);

    // 최근 댓글 (도배 방지)
    List<Comment> findTop2ByPostIdAndUserIdOrderByCreatedAtDesc(Long postId, Long userId);

    // 특정 기간에 작성된 댓글
    List<Comment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 유저의 댓글
    List<Comment> findByUserId(Long userId);

    // 특정 게시글의 댓글 삭제
    void deleteByPostId(Long postId);

}