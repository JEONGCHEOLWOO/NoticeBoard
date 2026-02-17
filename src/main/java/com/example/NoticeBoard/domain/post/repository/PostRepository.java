package com.example.NoticeBoard.domain.post.repository;

import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 조회(전체)
    Page<PostResponseDto> findAllPosts(Pageable pageable);

    // 게시글 조회(제목)
    Page<PostResponseDto> findByTitle(String keyword, Pageable pageable);

    // 게시글 조회(내용)
    Page<PostResponseDto> findByContent(String keyword, Pageable pageable);

    // 게시글 조회(작성자 닉네임)
    Page<PostResponseDto> findByNickname(String nickname, Pageable pageable);

    // 게시글 조회(제목 + 내용)
    Page<PostResponseDto> findByTitleAndContent(String keyword, Pageable pageable);

    // User의 게시글 목록 조회
    List<Post> findByUserId(Long userId);

    // 특정 상태의 게시글 수
    Long countByPostStatus(PostStatus status);
}
