package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글별 댓글 조회
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    // 특정 게시글에서 사용자의 GIF 개수
    long countByPostIdAndUserIdAndGifTrue(Long postId, Long userId);

    // 최근 댓글 (도배 방지)
    List<Comment> findTop2ByPostIdAndUserIdOrderByCreatedAtDesc(Long postId, Long userId);

    @Modifying
    @Query("""
        delete from Comment c
        where c.commentStatus = 'DELETED'
        and c.deletedAt < :expiredAt
    """)
    int hardDeleteExpiredComments(LocalDateTime expiredAt);
}
