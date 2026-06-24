package com.ssafy.yamyam.domain.video.dto;

import lombok.Data;

@Data
public class VideoRegisterDto {
    private String key;
    private Long teamId;
    private String mealType;
    private String mealDate;
    private String description;
}
