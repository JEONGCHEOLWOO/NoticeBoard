package com.example.NoticeBoard.domain.comment.service;

import com.example.NoticeBoard.domain.comment.dto.CommentResponseDto;
import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 댓글 전체 조회 (게시글 기준)
    public List<CommentResponseDto> getComments(Long postId, Long lastId){

        String cacheKey = "comment:list:" + postId + ":" + (lastId == null ? "first" : lastId);
        // GET comment:list:{postId}:{lastId} -> GET comment:list:12:20 -> 게시글 12에 있는 댓글의 20번째 이후의 댓글을 불러옴.
        // 캐시 조회
        List<CommentResponseDto> cachedComments = (List<CommentResponseDto>) redisTemplate.opsForValue().get(cacheKey);

        if(cachedComments != null){
            log.info("댓글 캐시 히트: postId={}, lastId={}", postId, lastId);
            return cachedComments;
        }

        log.info("댓글 캐시 미스: DB 조회 진행 postId={}, lastId={}", postId, lastId);

        // DB 조회
        List<Comment> comments;
        if(lastId == null){
            comments = commentRepository.findTop20ByPostIdOrderByIdDesc(postId);
        } else {
            comments = commentRepository.findTop20ByPostIdAndIdLessThanOrderByIdDesc(postId, lastId);
        }

        List<CommentResponseDto> result = comments.stream().map(CommentResponseDto::fromEntity).toList();

        // Redis에 캐시 저장 (TTL - 3분)
        redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(3));   // Value 저장
        redisTemplate.opsForSet().add("comment:list:keys:" + postId, cacheKey); // Key 저장
        return result;
    }
}
