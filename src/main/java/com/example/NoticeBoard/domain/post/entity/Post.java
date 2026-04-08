package com.example.NoticeBoard.domain.post.entity;

import com.example.NoticeBoard.global.enumeration.Category;
import com.example.NoticeBoard.global.enumeration.PostStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

// 게시글에 GIF를 넣지 못하는 이유는 동영상이라는 대체품이 있다.
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 내부 PK

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Category category;  // Enum: FREE, NOTICE, QNA

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 게시글 작성자 id

    private String nickname; // 스냅샷 필드

    @Column(nullable = false, length = 100)
    private String title; // 게시글 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 게시글 내용

    @Column(name = "image_url")
    private String imageUrl; // 이미지 주소

    @Column(name = "file_url")
    private String fileUrl; // 파일 주소

    @Enumerated(EnumType.STRING)
    @Column(name = "post_status", nullable = false, length = 10)
    private PostStatus postStatus; // 게시글 종류 (일반 게시글, 삭제된 게시글, 블라인드된 게시글 등)

    @Column(name = "view_count", nullable = false)
    private Long viewCount; // 조회수

    @Column(name = "comment_count", nullable = false)
    private Long commentCount; // 댓글 수

    @Column(name = "like_count", nullable = false)
    private Long likeCount; // 좋아요 수 -> 캐시 용도

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 게시글 등록 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 게시글 업데이트 날짜
    
    private LocalDateTime deletedAt; // 게시글 삭제 요청 날짜

    // ------------------비즈니스 메소드-------------------
    // 비즈니스 메소드를 해야될까?
    // 도메인 주도 설계를 하게 되면, 서비스는 엔터티를 호출하는 정도의 얇은 비즈니스 로직을 가지게 됨.
    // 하지만 엔터티를 단순히 데이터를 전달하는 역할로 사용하고, 서비스에 비즈니스 로직을 두어도 된다.
    // 엔터티에 비즈니스 로직을 설계하면 되는 경우 객체로 사용하는 것이고, 서비스에 비즈니스 로직을 설계하면 엔터티는 자료구조로 사용하는 방식이 된다.
    // 둘중 옳은 방법은 없다. 프로젝트에 맞는 방식으로 설계를 하면 된다.
    // 그러면 내 현재 설계는 도메인 주도 설계 -> MSA 설계로 넘거갈 예정이니 도메인 주도 설계에 초점을 두어 비즈니스 메소드를 엔터티에 두는것이 좋을꺼 같다.

    // 변경된 사항이 있는지 확인
    public boolean isChanged(String title, String content, String imageUrl, String fileUrl, Category category, PostStatus postStatus){
        boolean changed = false;

        if (!Objects.equals(this.title, title)) {
            this.title = title;
            changed = true;
        }

        if (!Objects.equals(this.content, content)) {
            this.content = content;
            changed = true;
        }

        if (!Objects.equals(this.category, category)) {
            this.category = category;
            changed = true;
        }

        if (!Objects.equals(this.postStatus, postStatus)) {
            this.postStatus = postStatus;
            changed = true;
        }

        if (!Objects.equals(this.imageUrl, imageUrl)) {
            this.imageUrl = imageUrl;
            changed = true;
        }

        if (!Objects.equals(this.fileUrl, fileUrl)) {
            this.fileUrl = fileUrl;
            changed = true;
        }

        return changed;
    }

    // 게시글 삭제 (Soft Delete)
    public void delete(){
        this.postStatus = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    // 삭제된 게시글인지 확인
    // 왜 == 으로 했냐? equals가 아니라 => JVM에서는 enum은 싱글톤 인스턴스로 관리됨. 따라서 같은 객체인지 비교(reference 비교)하는 == 이 더 적합함. 값을 비교하는 equals 는 x.
    // "equals"를 쓰면 PostStatus가 null 이면 예외 발생가 발생하지만, "=="으로 사용하면 false를 반환함.
    public boolean isDeleted() {
        return this.postStatus == PostStatus.DELETED || this.deletedAt != null;
    }
}
