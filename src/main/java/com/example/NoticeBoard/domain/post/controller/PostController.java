package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.global.security.CustomUserDetails;
import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.service.PostService;
import com.example.NoticeBoard.domain.report.dto.PostReportRequestDto;
import lombok.RequiredArgsConstructor;
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
    @GetMapping("/search/all")
    public List<PostResponseDto> getAllPosts(){
        return postService.getAllPosts();
    }

    // 게시글 조회(제목)
    @GetMapping("/search/title")
    public ResponseEntity<List<PostResponseDto>> sarchByTitle(@RequestParam String keyword){
        return ResponseEntity.ok(postService.searchByTitle(keyword));
    }

    // 게시글 조회(내용)
    @GetMapping("/search/content")
    public ResponseEntity<List<PostResponseDto>> searchByContent(@RequestParam String keyword){
        return ResponseEntity.ok(postService.searchByContent(keyword));
    }

    // 게시글 조회(작성자 닉네임)
    @GetMapping("/search/nickname")
    public ResponseEntity<List<PostResponseDto>> searchByNickname(@RequestParam String keyword){
        return ResponseEntity.ok(postService.searchByNickname(keyword));
    }

    // 게시글 조회(제목 + 내용)
    @GetMapping("/search/title-content")
    public ResponseEntity<List<PostResponseDto>> searchByTitleOrContent(@RequestParam String keyword){
        return ResponseEntity.ok(postService.searchByTitleOrContent(keyword));
    }

    // 게시글 좋아요
    @PostMapping("/like/{postId}")
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.likePost(postId, userDetails.getId());
        return ResponseEntity.ok().build();
    }
    
    // 게시글 좋아요 취소
    @PostMapping("/unlike/{postId}")
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        postService.unlikePost(postId, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    // 게시글 신고
    @PostMapping("/report/{postId}")
    public ResponseEntity<Void> reportPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostReportRequestDto postReportRequestDto){
        postService.reportPost(postId, userDetails.getId(), postReportRequestDto);
        return ResponseEntity.ok().build();
    }

}
