package com.ssafy.yamyam.domain.video.dto;

import lombok.Data;

@Data
public class NutrientGoalDto {
    private double targetCalories;
    private double targetCarbs;
    private double targetProtein;
    private double targetFat;
}