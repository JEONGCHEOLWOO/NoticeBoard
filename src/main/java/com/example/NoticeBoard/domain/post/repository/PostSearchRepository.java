package com.example.NoticeBoard.domain.post.repository;

import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostSearchDocument, Long> {
    Page<PostSearchDocument> searchByCondition(String keyword, String type, Pageable pageable);
}
