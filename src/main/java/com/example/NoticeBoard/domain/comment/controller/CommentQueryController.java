package com.example.NoticeBoard.domain.comment.controller;

import com.example.NoticeBoard.domain.comment.dto.CommentResponseDto;
import com.example.NoticeBoard.domain.comment.service.CommentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments/query")
@RequiredArgsConstructor
@Slf4j
public class CommentQueryController {

    private final CommentQueryService commentQueryService;

    // 댓글 조회 (상위 20개씩)
    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @RequestParam Long postId,
            @RequestParam(required = false) Long lastId){
        log.info("댓글 조회 요청: postId={}, lastId={}", postId, lastId);
        return ResponseEntity.ok(commentQueryService.getComments(postId, lastId));
    }

}
