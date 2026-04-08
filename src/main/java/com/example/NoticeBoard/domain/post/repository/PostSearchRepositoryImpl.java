package com.example.NoticeBoard.domain.post.repository;

import com.example.NoticeBoard.domain.post.entity.PostSearchDocument;
import com.example.NoticeBoard.global.enumeration.SearchType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
public class PostSearchRepositoryImpl implements PostSearchRepositoryCustom{

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<PostSearchDocument> searchByCondition(String keyword, SearchType searchType, Pageable pageable){
        Query query = buildQuery(keyword, searchType, pageable);

        SearchHits<PostSearchDocument> searchHits = elasticsearchOperations.search(query, PostSearchDocument.class);
        List<PostSearchDocument> content = searchHits.stream()
                .map(SearchHit::getContent)
                .toList();

        long total = searchHits.getTotalHits();
        return new PageImpl<>(content, pageable, total);
    }

    private Query buildQuery(String keyword, SearchType searchType, Pageable pageable){

        Criteria criteria = new Criteria();

        if (keyword != null && !keyword.isEmpty()){
            switch (searchType) {
                case TITLE:
                    criteria = Criteria.where("title").contains(keyword);
                    break;
                case CONTENT:
                    criteria = Criteria.where("content").contains(keyword);
                    break;
                case WRITER:
                    criteria = Criteria.where("userId").contains(keyword); // nickname이 더 자연스러움
                    break;
                case TITLE_CONTENT:
                    criteria = new Criteria()
                            .or(Criteria.where("title").contains(keyword))
                            .or(Criteria.where("content").contains(keyword));
                    break;
            }
        }

        criteria = criteria.and(criteria.where("isDeleted").is(false));

        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(pageable);

        return query;
    }

}
