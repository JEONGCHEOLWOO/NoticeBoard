package com.example.NoticeBoard.service;

import com.example.NoticeBoard.dto.PostRequestDto;
import com.example.NoticeBoard.dto.PostResponseDto;
import com.example.NoticeBoard.entity.Post;
import com.example.NoticeBoard.entity.User;
import com.example.NoticeBoard.repository.PostRepository;
import com.example.NoticeBoard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 작성 (나중에 예외 처리를 RuntimeException, IllegalArgumentException 말고 자세히 할 필요가 있음)
    public PostResponseDto createPost(Long userId, PostRequestDto requestDto){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("해당 회원을 찾지 못했습니다."));

        Post post = Post.builder()
                .category(requestDto.getCategory())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .user(user)
                .build();

//        디버깅
//        Post saved = postRepository.save(post);
//        System.out.println(saved);
//        return PostResponseDto.fromEntity(saved);

        return PostResponseDto.fromEntity(postRepository.save(post));
    }

    // 게시글 수정
    public PostResponseDto updatePost(Long postId, Long userId, PostRequestDto requestDto){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글을 찾지 못했습니다."));

        // 작성자 본인인지 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        post.setCategory(requestDto.getCategory());
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        return PostResponseDto.fromEntity(post);
    }

    // 게시글 삭제
    public void deletePost(Long postId, Long userId){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("해당 게시글을 찾지 못했습니다."));

        // 작성자 본인인지 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }
        postRepository.deleteById(postId);
    }

    // 게시글 조회(전체)
    public List<PostResponseDto> getAllPosts(){
        return postRepository.findAll()
                .stream()
                .map(PostResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 게시글 조회(제목)
    public List<PostResponseDto> searchByTitle(String keyword){
        return postRepository.findByTitleContaining(keyword)
                .stream()
                .map(PostResponseDto::fromEntity)
                .toList();
    }

    // 게시글 조회(내용)
    public List<PostResponseDto> searchByContent(String keyword){
        return postRepository.findByContentContaining(keyword)
                .stream()
                .map(PostResponseDto::fromEntity)
                .toList();
    }

    // 게시글 조회(작성자)
    public List<PostResponseDto> searchByNickname(String keyword){
        return postRepository.findByUser_NicknameContaining(keyword)
                .stream()
                .map(PostResponseDto::fromEntity)
                .toList();
    }

    // 게시글 조회(제목 + 내용)
    public List<PostResponseDto> searchByTitleOrContent(String keyword){
        return postRepository.findByTitleContainingOrContentContaining(keyword, keyword)
                .stream()
                .map(PostResponseDto::fromEntity)
                .toList();
    }
}
