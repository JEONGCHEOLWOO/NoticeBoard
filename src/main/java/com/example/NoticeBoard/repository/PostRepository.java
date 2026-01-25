package com.example.NoticeBoard.repository;

import com.example.NoticeBoard.entity.Post;
import com.example.NoticeBoard.enumeration.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 조회(제목)
    List<Post> findByTitleContaining (String keyword);

    // 게시글 조회(내용)
    List<Post> findByContentContaining (String keyword);

    // 게시글 조회(작성자 닉네임)
    // User_Nickname → Post 엔티티에서 User와 연관관계가 있기 때문에 JPA가 Join을 자동으로 처리해서 닉네임 검색 가능.
    List<Post> findByUser_NicknameContaining (String nickname);

    // 게시글 조회(제목 + 내용)
    List<Post> findByTitleContainingOrContentContaining (String titleKeyword, String contentKeyword);

    // 특정 기간에 작성된 게시글
    List<Post> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 특정 유저의 게시글
    List<Post> findByUserId(Long userId);

    // 특정 상태의 게시글 수
    Long countByPostStatus(PostStatus status);
}
