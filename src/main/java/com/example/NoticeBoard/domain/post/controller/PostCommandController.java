package com.example.NoticeBoard.domain.post.controller;

import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.service.PostCommandService;
import com.example.NoticeBoard.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/command")
@RequiredArgsConstructor
@Slf4j
public class PostCommandController {

    private final PostCommandService postCommandService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostRequestDto postRequestDto){
        log.info("게시글 작성 요청: userId={}, category={}", userDetails.getId(), postRequestDto.getCategory());
        return ResponseEntity.ok(postCommandService.createPost(userDetails.getId(), postRequestDto));
    }

    // 게시글 수정
    @PutMapping ("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PostRequestDto postRequestDto){
        log.info("게시글 수정 요청: postId={}, userId={}", postId, userDetails.getId());
        return ResponseEntity.ok(postCommandService.updatePost(postId, userDetails.getId(), postRequestDto));
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        log.info("게시글 삭제 요청: postId={}, userId={}", postId, userDetails.getId());
        postCommandService.deletePost(postId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
