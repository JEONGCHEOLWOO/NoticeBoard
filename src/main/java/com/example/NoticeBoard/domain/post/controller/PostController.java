package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.global.security.CustomUserDetails;
import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping("/create")
    public ResponseEntity<PostResponseDto> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostRequestDto postRequestDto){
        log.info("게시글 작성 요청: userId={}, category={}", userDetails.getId(), postRequestDto.getCategory());
        return ResponseEntity.ok(postService.createPost(userDetails.getId(), postRequestDto));
    }

    // 게시글 수정
    @PostMapping ("/update/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostRequestDto postRequestDto){
        log.info("게시글 수정 요청: postId={}, userId={}", postId, userDetails.getId());
        return ResponseEntity.ok(postService.updatePost(postId, userDetails.getId(), postRequestDto));
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("게시글 삭제 요청: postId={}, userId={}", postId, userDetails.getId());
        postService.deletePost(postId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // 게시글 조회 (내용)
    @GetMapping("/find/{postId}")
    public ResponseEntity<PostResponseDto> getPostDetail(@PathVariable Long postId){
        log.info("게시글 상세 조회: postId={}", postId);
        postService.incrementViewCount(postId);
        return ResponseEntity.ok(postService.getPostDetail(postId));
    }

    // 게시글 조회(전체 최신순)
    @GetMapping("/find/all")
    public ResponseEntity<Page<PostResponseDto>> findAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        log.info("최신 게시글 조회: page={}, size={}", page, size);
        return ResponseEntity.ok(postService.findAllPosts(page,size));
    }

    // 게시글 검색 (제목, 내용, 제목 + 내용, 작성자)
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponseDto>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "titleAndContent") String type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable){
        log.info("게시글 검색: keyword={}, type={}", keyword, type);
        return ResponseEntity.ok(postService.searchPosts(keyword, type, pageable));
    }



//    // 게시글 좋아요
//    @PostMapping("/like/{postId}")
//    public ResponseEntity<Void> likePost(
//            @PathVariable Long postId,
//            @AuthenticationPrincipal CustomUserDetails userDetails){
//        postService.likePost(postId, userDetails.getId());
//        return ResponseEntity.ok().build();
//    }
//
//    // 게시글 좋아요 취소
//    @PostMapping("/unlike/{postId}")
//    public ResponseEntity<Void> unlikePost(
//            @PathVariable Long postId,
//            @AuthenticationPrincipal CustomUserDetails userDetails){
//        postService.unlikePost(postId, userDetails.getId());
//        return ResponseEntity.ok().build();
//    }

//    // 게시글 신고
//    @PostMapping("/report/{postId}")
//    public ResponseEntity<Void> reportPost(
//            @PathVariable Long postId,
//            @AuthenticationPrincipal CustomUserDetails userDetails,
//            @RequestBody PostReportRequestDto postReportRequestDto){
//        postService.reportPost(postId, userDetails.getId(), postReportRequestDto);
//        return ResponseEntity.ok().build();
//    }

}
