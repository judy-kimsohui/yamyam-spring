package com.ssafy.yamyam.domain.nutrition.model;

import lombok.Data;

@Data
public class RecognizedFoodItem {

    private Long id;

    // nutrition_analysis 테이블 FK
    private Long nutritionAnalysisId;

    private String foodName;
    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
}
