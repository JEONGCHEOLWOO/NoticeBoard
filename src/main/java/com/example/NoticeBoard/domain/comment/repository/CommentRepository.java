package com.example.NoticeBoard.domain.comment.repository;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 최근 댓글 (도배 방지)
    List<Comment> findTop2ByPostIdAndUserIdOrderByCreatedAtDesc(Long postId, Long userId);

    // 댓글 조회 (첫 20개 댓글)
    List<Comment> findTop20ByPostIdOrderByIdDesc(Long postId);

    // 댓글 조회 (cursor)
    List<Comment> findTop20ByPostIdAndIdLessThanOrderByIdDesc(Long postId, Long lastId);
}