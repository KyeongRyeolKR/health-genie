package com.example.healthgenie.boundedContext.community.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostRequest {

    private Long postId;
    private Long writerId;
    private String title;
    private String content;
    private List<MultipartFile> photos;
}