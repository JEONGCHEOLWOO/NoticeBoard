package com.example.NoticeBoard.domain.post.repository;

import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.global.enumeration.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostSearchRepositoryCustom {
    Page<PostSearchDocument> searchByCondition(String keyword, SearchType searchType, Pageable pageable);
}
