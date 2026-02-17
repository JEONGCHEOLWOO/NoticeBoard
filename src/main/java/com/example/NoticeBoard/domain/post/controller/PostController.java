package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.global.security.CustomUserDetails;
import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping("/create")
    public ResponseEntity<PostResponseDto> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostRequestDto postRequestDto){
        return ResponseEntity.ok(postService.createPost(userDetails.getId(), postRequestDto));
    }

    // 게시글 수정
    @PostMapping ("/update/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostRequestDto postRequestDto){
        return ResponseEntity.ok(postService.updatePost(postId, userDetails.getId(), postRequestDto));
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.deletePost(postId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
    
    // 게시글 조회(전체)
    @GetMapping("/find/all")
    public ResponseEntity<Page<PostResponseDto>> findAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(postService.findAllPosts(page,size));
    }

    // 게시글 조회(제목)
    @GetMapping("/find/title")
    public ResponseEntity<Page<PostResponseDto>> findByTitle(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(postService.findByTitle(keyword, page, size));
    }

    // 게시글 조회(내용)
    @GetMapping("/find/content")
    public ResponseEntity<Page<PostResponseDto>> findByContent(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(postService.findByContent(keyword, page, size));
    }

    // 게시글 조회(작성자 닉네임)
    @GetMapping("/find/nickname")
    public ResponseEntity<Page<PostResponseDto>> findByNickname(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(postService.findByNickname(keyword, page, size));
    }

    // 게시글 조회(제목 + 내용)
    @GetMapping("/find/title-content")
    public ResponseEntity<Page<PostResponseDto>> findByTitleOrContent(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        return ResponseEntity.ok(postService.findByTitleAndContent(keyword, page, size));
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
