package com.example.NoticeBoard.controller;

import com.example.NoticeBoard.dto.PostRequestDto;
import com.example.NoticeBoard.dto.PostResponseDto;
import com.example.NoticeBoard.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping("/create")
    public ResponseEntity<PostResponseDto> createPost(@RequestParam Long userId, @RequestBody PostRequestDto requestDto){
        return ResponseEntity.ok(postService.createPost(userId, requestDto));
    }

    // 게시글 수정
    @PostMapping ("/update/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long postId, @RequestParam Long userId, @RequestBody PostRequestDto requestDto){
        return ResponseEntity.ok(postService.updatePost(postId, userId, requestDto));
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, @RequestParam Long userId){
        postService.deletePost(postId, userId);
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

}
