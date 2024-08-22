package com.gongcha.berrymatch.postFile;

import com.gongcha.berrymatch.post.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class PostFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Post post;

    private String originalFileName;

    private String fileType;

    private Long size;

    private String fileKey;

    private String fileUrl;

    @Builder
    public PostFile(String originalFileName, String storedFileName, String fileType, Long size, String fileKey, String fileUrl) {
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.size = size;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }
}
