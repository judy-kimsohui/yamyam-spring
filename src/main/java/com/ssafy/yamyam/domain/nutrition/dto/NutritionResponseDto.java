package com.ssafy.yamyam.domain.nutrition.dto;

import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import lombok.Data;

import java.util.List;

@Data
public class NutritionResponseDto {

    private Long videoId;
    private String status; // PENDING, DONE, FAILED

    // status = DONE 일 때만 채워짐
    private Double totalCalories;
    private Double totalCarbs;
    private Double totalProtein;
    private Double totalFat;
    private List<FoodItemDto> foods;

    // status = FAILED 일 때만 채워짐
    private String errorMessage;

    @Data
    public static class FoodItemDto {
        private String foodName;
        private Double calories;
        private Double carbs;
        private Double protein;
        private Double fat;
    }

    public static NutritionResponseDto pending(Long videoId) {
        NutritionResponseDto dto = new NutritionResponseDto();
        dto.setVideoId(videoId);
        dto.setStatus(NutritionAnalysis.Status.PENDING.name());
        return dto;
    }

    public static NutritionResponseDto failed(Long videoId, String errorMessage) {
        NutritionResponseDto dto = new NutritionResponseDto();
        dto.setVideoId(videoId);
        dto.setStatus(NutritionAnalysis.Status.FAILED.name());
        dto.setErrorMessage(errorMessage);
        return dto;
    }
}
