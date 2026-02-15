package com.example.NoticeBoard.domain.post.service;

import com.example.NoticeBoard.domain.post.dto.PostRequestDto;
import com.example.NoticeBoard.domain.post.dto.PostResponseDto;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.report.dto.PostReportRequestDto;
import com.example.NoticeBoard.domain.report.entity.PostReport;
import com.example.NoticeBoard.domain.post.entity.Post;
import com.example.NoticeBoard.domain.post.entity.PostLike;
import com.example.NoticeBoard.domain.user.entity.User;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import com.example.NoticeBoard.global.enumeration.ReportStatus;
import com.example.NoticeBoard.domain.post.repository.PostLikeRepository;
import com.example.NoticeBoard.domain.report.repository.PostReportRepository;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 작성 (나중에 예외 처리를 RuntimeException, IllegalArgumentException 말고 자세히 할 필요가 있음)
    public PostResponseDto createPost(Long userId, PostRequestDto requestDto){
        if(!userRepository.existsById(userId)){
            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        Post post = Post.builder()
                .category(requestDto.getCategory())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .imageUri(requestDto.getImageUri())
                .fileUri(requestDto.getFileUri())
                .userId(userId)
                .postStatus(PostStatus.NORMAL)
                .likeCount(0)
                .build();

        return PostResponseDto.fromEntity(postRepository.save(post));
    }

    // 게시글 수정 - 본인이 작성한 게시글 일때만 수정 버튼 생성 및 수정 가능
    public PostResponseDto updatePost(Long postId, Long userId, PostRequestDto requestDto){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        // 삭제된 게시글인지 확인
        // 왜 == 으로 했냐? equals가 아니라 => JVM에서는 enum은 싱글톤 인스턴스로 관리됨. 따라서 같은 객체인지 비교(reference 비교)하는 == 이 더 적합함. 값을 비교하는 equals 는 x.
        // "equals"를 쓰면 PostStatus가 null 이면 예외 발생가 발생하지만, "=="으로 사용하면 false를 반환함.
        if (post.getPostStatus() == PostStatus.DELETED) {
            throw new IllegalArgumentException("삭제된 게시글은 수정할 수 없습니다.");
        }

        post.setCategory(requestDto.getCategory());
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        if (requestDto.getImageUri() != null) {
            post.setImageUri(requestDto.getImageUri());
        }

        if (requestDto.getFileUri() != null) {
            post.setFileUri(requestDto.getFileUri());
        }

        return PostResponseDto.fromEntity(post);
    }

    // ------------------------------------- 여기서 부터 수정 필요---------------
    // 게시글 삭제 (Soft Delete)
    public void deletePost(Long postId, Long userId){
        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new RuntimeException("해당 게시글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        // 삭제된 게시글인지 확인
        if (post.getPostStatus() == PostStatus.DELETED) {
            throw new IllegalArgumentException("삭제된 게시글은 수정할 수 없습니다.");
        }

        post.setDeletedAt(LocalDateTime.now());
        post.setPostStatus(PostStatus.DELETED);
    }

    // 게시글 조회(전체)
    public List<PostResponseDto> searchAllPosts(){
        return postRepository.findAll()
                .stream()
                .map(PostResponseDto::fromEntity)
                .toList();
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

//    // 게시글 좋아요
//    public void likePost(Long postId, Long userId) {
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
//
//        if(!userRepository.existsById(userId)){
//            throw new IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
//        }
//
//        PostLike postLike = PostLike.builder()
//                .postId(postId)
//                .userId(userId)
//                .build();
//
//        postLikeRepository.save(postLike);
//
//        post.setLikeCount(post.getLikeCount() + 1);
//    }
//
//    // 게시글 좋아요 취소
//    public void unlikePost(Long postId, Long userId) {
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
//
//        PostLike postLike = postLikeRepository.findByPostIdAndUserId(postId, userId)
//                .orElseThrow(() -> new IllegalStateException("좋아요 기록이 없습니다."));
//
//        postLikeRepository.delete(postLike);
//
//        post.setLikeCount(post.getLikeCount() - 1);
//    }


//    // 게시글 신고
//    public void reportPost(Long postId, Long userId, PostReportRequestDto requestDto) {
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));
//
//        if (postReportRepository.existsByPostIdAndUserId(postId, userId)) {
//            throw new IllegalStateException("이미 신고한 게시글입니다.");
//        }
//
//        if (post.getUserId().equals(userId)) {
//            throw new IllegalArgumentException("본인이 작성한 게시글은 신고할 수 없습니다.");
//        }
//
//        PostReport report = PostReport.builder()
//                .post(post)
//                .user(user)
//                .content(requestDto.getContent())
//                .reportReason(requestDto.getReason())
//                .reportStatus(ReportStatus.PROCESSING)
//                .build();
//
//        postReportRepository.save(report);
//
//        // 신고 5회 이상 → 자동 블라인드
//        if (post.getReports().size() >= 5) {
//            post.setPostStatus(PostStatus.BLIND);
//        }
//    }

}
