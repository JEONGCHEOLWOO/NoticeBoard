package com.example.NoticeBoard.domain.comment.service;

import com.example.NoticeBoard.domain.comment.entity.Comment;
import com.example.NoticeBoard.domain.comment.event.CommentEventProducer;
import com.example.NoticeBoard.domain.comment.repository.CommentRepository;
import com.example.NoticeBoard.domain.comment.dto.CommentResponseDto;
import com.example.NoticeBoard.domain.comment.dto.CommentRequestDto;
import com.example.NoticeBoard.global.enumeration.CommentStatus;
import com.example.NoticeBoard.domain.post.repository.PostRepository;
import com.example.NoticeBoard.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentCommandService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final CommentEventProducer commentEventProducer;

    private static final List<String> ALLOWED_GIF_DOMAINS = List.of(
            "media.giphy.com",
            "tenor.com",
            "media.tenor.com"
    );

    private static final String SIGNATURE_SECRET = "my-secret-key";
    public static final String COMMENT_DETAIL = "comment:detail:";



    // 댓글 생성
    public CommentResponseDto createComment(Long postId, Long userId, CommentRequestDto commentRequestDto) {
        
        postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        // 이미지/ GIF 정책 확인 - 하나의 댓글에는 하나의 이미지나 GIF 만 가능하고, 이미지와 GIF는 동시에 업로드 할 수 없음.
        validateMedia(commentRequestDto);
        validateGifUrl(commentRequestDto.getGifUrl());

        if (commentRequestDto.getGifUrl() != null) {
            commentRequestDto.setGifUrl(generateSignedUrl(commentRequestDto.getGifUrl()));
        }

        // 연속 댓글 제한 (60초) - 도배 위험
        List<Comment> recent = commentRepository.findTop2ByPostIdAndUserIdOrderByCreatedAtDesc(postId, userId);

        // 댓글은 60초에 1개만
        if (!recent.isEmpty()) {
            LocalDateTime last = recent.get(0).getCreatedAt();
            if (Duration.between(last, LocalDateTime.now()).getSeconds() < 60){
                throw new IllegalStateException("댓글은 60초에 1개만 작성할 수 있습니다.");
            }
        }

        // 댓글 1개당 이미지 1개
        if (commentRequestDto.getImageUrl() != null && commentRequestDto.getImageUrl().contains(",")) {
            throw new IllegalArgumentException("댓글에는 이미지를 1개만 첨부할 수 있습니다.");
        }

        // 부모 댓글 확인 - 부모 댓글이 있으면 대댓글, 없으면 게시글 댓글
        Long parentId = null;
        if (commentRequestDto.getParentId() != null) {
            Comment parent = commentRepository.findById(commentRequestDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 없습니다."));
            parentId = parent.getId();
        }
        
        Comment comment = Comment.builder()
                .postId(postId)
                .userId(userId)
                .parentId(parentId)
                .content(commentRequestDto.getContent())
                .imageUrl(commentRequestDto.getImageUrl())
                .gifUrl(commentRequestDto.getGifUrl())
                .commentStatus(CommentStatus.NORMAL)
                .likeCount(0L)
                .build();

        Comment savedComment = commentRepository.save(comment);

        commentEventProducer.sendCommentCreatedEvent(savedComment.getId());
        log.info("댓글 생성 완료: commentId={}, parentId={}, postId={}, userId={}", savedComment.getId(), savedComment.getParentId(), savedComment.getPostId(), savedComment.getUserId());

        return CommentResponseDto.fromEntity(savedComment);
    }

    // 댓글 수정 - IllegalArgumentException 와 IllegalStateException 차이
    public CommentResponseDto updateComment(Long postId, Long commentId, Long userId, CommentRequestDto commentRequestDto){
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 부모 댓글이 삭제된 댓글인지 확인
        // 삭제된 댓글인지 확인
        if (comment.isDeleted()){
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        validateMedia(commentRequestDto);
        validateGifUrl(commentRequestDto.getGifUrl());

        if (commentRequestDto.getGifUrl() != null) {
            commentRequestDto.setGifUrl(generateSignedUrl(commentRequestDto.getGifUrl()));
        }

        // 비즈니스 메소드(comment 엔터티)
        comment.update(commentRequestDto.getContent(), commentRequestDto.getImageUrl(), commentRequestDto.getGifUrl());

        // Redis 기존 캐시 삭제
        evictCommentCache(commentId);
        
        // kafka 이벤트 생성
        commentEventProducer.sendCommentUpdateEvent(commentId);
        log.info("댓글 수정 완료: commentId={}, parentId={}, postId={}, userId={}", commentId, comment.getParentId(), postId, userId);

        return CommentResponseDto.fromEntity(comment);

    }

    // 댓글 삭제
    public void deleteComment(Long postId, Long commentId, Long userId) {

        postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 비즈니스 메소드 (Comment 엔터티) -> commentStatus 를 NORMAL -> DELETED 변경
        comment.delete();

        // Redis 기존 캐시 삭제
        evictCommentCache(commentId);

        // kafka 이벤트 생성
        commentEventProducer.sendCommentDeleteEvent(commentId);
        log.info("댓글 삭제 완료: commentId={}, parentId={}, postId={}, userId={}", commentId, comment.getParentId(), postId, userId);
    }

    // Redis 캐시 삭제 - 데이터가 수정되거나 삭제되면 캐시를 제거해서 데이터 불일치를 방지시킴.
    private void evictCommentCache(Long commentId){
        redisTemplate.delete(COMMENT_DETAIL + commentId);
    }

    // 댓글 이미지/GIF 검증
    private void validateMedia(CommentRequestDto commentRequestDto){

        boolean hasImage = commentRequestDto.getImageUrl() != null;
        boolean hasGif = commentRequestDto.getGifUrl() != null;

        if (hasImage && hasGif){
            throw new IllegalArgumentException("이미지와 GIF 둘중 하나만 업로드 할 수 있습니다.");
        }

        if (hasImage && commentRequestDto.getImageUrl().contains(",")){
            throw new IllegalArgumentException("1개의 이미지만 업로드 가능합니다.");
        }

        if (hasGif && commentRequestDto.getGifUrl().contains(",")){
            throw new IllegalArgumentException("1개의 GIF만 업로드 가능합니다.");
        }
    }
    
    // 허용된 GIF URL 확인 - GIF 도메인 검증
    private void validateGifUrl(String gifUrl) {

        if (gifUrl == null) return;

        boolean valid = ALLOWED_GIF_DOMAINS.stream()
                .anyMatch(gifUrl::contains);

        if (!valid){
            throw new IllegalArgumentException("허용되지 않은 GIF URL 입니다.");
        }
    }

    // Signed URL 생성 - 복사 방지
    private String generateSignedUrl(String gifUrl) {

        long expires = System.currentTimeMillis() + 60000;

        String data = gifUrl + expires + SIGNATURE_SECRET;

        String signature = Integer.toHexString(data.hashCode());

        return "/api/gif?url=" + gifUrl + "&expires=" + expires + "&sig" + signature;
    }

}
