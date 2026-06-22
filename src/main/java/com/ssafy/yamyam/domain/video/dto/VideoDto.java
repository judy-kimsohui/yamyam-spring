package com.ssafy.yamyam.domain.video.dto;

import lombok.Data;

@Data
public class VideoDto {
    private Long id;
    private Long userId;
    private String uploaderNickName;
    private Long teamId;
    private String mealType;
    private String mealDate;
    private String videoUrl;
    private String description;
    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private String aiComment;
    private String createdAt;
    private int likeCount;
    private boolean liked;

    private String status; // PENDING, DONE, FAILED 등
}
