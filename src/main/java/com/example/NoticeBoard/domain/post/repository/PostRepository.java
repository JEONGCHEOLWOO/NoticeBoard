package com.example.NoticeBoard.domain.post.repository;

import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // User의 게시글 목록 조회
    List<Post> findByUserId(Long userId);

    // 특정 상태의 게시글 조회
    Page<Post> findByPostStatus(PostStatus postStatus, Pageable pageable);

    // 게시글 좋아요 수 증가
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + :count WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId, @Param("count") int count);

    // 조회수 증가
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + :count WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId, @Param("count") Integer count);

    // 사용자가 삭제 요청을 한 후 30일이 지나 DB에서 데이터 삭제 (Hard Delete)
    @Modifying
    @Query("""
    SELECT p.id
    FROM Post p
    WHERE p.postStatus = 'DELETED'
    AND p.deletedAt < :deletereqday
    """)
    List<Long> findDeletedPostIdsBefore(LocalDateTime deletereqday);
}
