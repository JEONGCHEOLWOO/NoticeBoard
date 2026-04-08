package com.example.NoticeBoard.domain.post.repository;

import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostSearchDocument, Long>, PostSearchRepositoryCustom {
}
