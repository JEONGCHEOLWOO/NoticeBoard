package com.example.NoticeBoard.domain.comment.service;

import com.example.NoticeBoard.domain.comment.dto.CommentResponseDto;
import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 댓글 조회 (게시글 기준)
    public List<CommentResponseDto> getComments(Long postId, Long lastId){

        String cacheKey = "comment:list:" + postId + ":" + (lastId == null ? "first" : lastId);

        // 캐시 조회
        List<CommentResponseDto> cachedComments = (List<CommentResponseDto>) redisTemplate.opsForValue().get(cacheKey);

        if(cached != null){
            log.info("댓글 캐시 히트: postId={}, lastId={}", postId, lastId);
            return cached;
        }

        // DB 조회
        log.info("댓글 캐시 미스: DB 조회 진행 postId={}, lastId={}", postId, lastId);

        List<Comment> comments;

        if(lastId == null){
            comments = commentRepository.findTop20ByPostIdOrderByIdDesc(postId);
        } else {
            comments = commentRepository.findTop20ByPostIdAndIdLessThanOrderByIdDesc(postId, lastId);
        }

        List<CommentResponseDto> result = comments.stream().map(CommentResponseDto::fromEntity).toList();

        // Redis에 캐시 저장 (TTL - 5분)
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(5));

        return result;
    }
}
