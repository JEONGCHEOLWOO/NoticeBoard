package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.dto.PostSearchResponseDto;
import com.example.NoticeBoard.domain.post.service.PostQueryService;
import com.example.NoticeBoard.global.enumeration.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/query")
@RequiredArgsConstructor
@Slf4j
public class PostQueryController {

    private final PostQueryService postQueryService;

    // 게시글 조회 (내용)
    @GetMapping("/find/{postId}")
    public ResponseEntity<PostResponseDto> getPostDetail(@PathVariable Long postId){
        log.info("게시글 상세 조회 요청: postId={}", postId);
        log.info("게시글 조회수 증가 요청: postId={}", postId);
        postQueryService.incrementViewCount(postId);
        return ResponseEntity.ok(postQueryService.getPostDetail(postId));
    }

    // 게시글 조회(전체 최신순)
    @GetMapping("/find/all")
    public ResponseEntity<Page<PostSearchResponseDto>> findAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        log.info("전체 게시글 조회 요청: page={}, size={}", page, size);
        return ResponseEntity.ok(postQueryService.findAllPosts(page,size));
    }

    // 게시글 검색 (제목, 내용, 제목 + 내용, 작성자)
    @GetMapping("/search")
    public ResponseEntity<Page<PostSearchResponseDto>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "titleAndContent") SearchType searchType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable){
        log.info("게시글 검색 요청: keyword={}, type={}", keyword, searchType);
        return ResponseEntity.ok(postQueryService.searchPosts(keyword, searchType, pageable));
    }

}
