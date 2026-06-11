package com.ssafy.yamyam.domain.video.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Video {
    private Long id;
    private Long userId;
    private Long teamId;
    private String mealType;   // BREAKFAST, LUNCH, DINNER
    private LocalDate mealDate;
    private String videoUrl;
    private String description;
    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private String aiComment;
    private LocalDateTime createdAt;
}
