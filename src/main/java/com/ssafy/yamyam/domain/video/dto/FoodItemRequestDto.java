package com.ssafy.yamyam.domain.video.dto;

import lombok.Data;

@Data
public class FoodItemRequestDto {
    private Long id;            // 수정/삭제 시 사용 (추가 시에는 null)
    private String foodName;    // 음식 이름
    private Double calories;    // 칼로리 (선택)
    private Double carbs;       // 탄수화물 (선택)
    private Double protein;     // 단백질 (선택)
    private Double fat;         // 지방 (선택)
}