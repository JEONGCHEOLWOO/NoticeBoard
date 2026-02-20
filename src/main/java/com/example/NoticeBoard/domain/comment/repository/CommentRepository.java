package com.example.NoticeBoard.domain.comment.repository;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.global.enumeration.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글 댓글 목록 조회
    List<Comment> findByPostId(Long postId);

    // User의 댓글 목록 조회
    List<Comment> findByUserId(Long userId);

    // 특정 게시글에서 사용자의 GIF 개수
    long countByPostIdAndUserIdAndGifTrue(Long postId, Long userId);

    // 최근 댓글 (도배 방지)
    List<Comment> findTop2ByPostIdAndUserIdOrderByCreatedAtDesc(Long postId, Long userId);

    // 게시글의 댓글 수 카운트
    Long countByPostId(Long postId);

    // 유저의 댓글 수 카운트
    Long countByUserId(Long userId);

    // 게시글의 댓글 수 카운트
    long countByPostIdAndCommentStatus(Long postId, CommentStatus commentStatus);

    // 게시글의 댓글 삭제
    void deleteByPostId(Long postId);

    // 대댓글 조회
    List<Comment> findByParentId(Long parentId);

}