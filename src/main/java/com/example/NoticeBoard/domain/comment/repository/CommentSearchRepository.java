package com.example.NoticeBoard.domain.comment.repository;

import com.example.NoticeBoard.domain.comment.entity.CommentSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface CommentSearchRepository extends ElasticsearchRepository<CommentSearchDocument, Long> {
}
