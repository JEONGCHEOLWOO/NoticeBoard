package com.example.NoticeBoard.entity;

import com.example.NoticeBoard.enumeration.FileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "file_upload")
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 내부 PK

    @ManyToOne
    @JoinColumn (name = "post_id", nullable = false)
    private Post post; // 어떤 게시글에 속한 파일인지 -> 게시글 id (Post PK)

    @Column(nullable = false)
    private String fileName; // 파일 이름

    @NotBlank
    @Column(nullable = false)
    private String filePath; // 파일 경로

    @Column(nullable = false)
    private String mimeType; // 실제 MIME 타입 (image/png, application/pdf 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileType fileType; // 파일 타입 Enum : IMAGE, VIDEO, DOCUMENT, AUDIO, OTHER

    private Long fileSize; // 파일 크키 (바이트)

    @CreationTimestamp
    private LocalDateTime uploadedAt;

}
