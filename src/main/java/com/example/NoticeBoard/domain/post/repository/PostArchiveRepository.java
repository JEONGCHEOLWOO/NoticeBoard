package com.example.NoticeBoard.domain.post.repository;

import com.example.NoticeBoard.domain.post.entity.PostArchive;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostArchiveRepository extends JpaRepository<PostArchive, Long> {
}
