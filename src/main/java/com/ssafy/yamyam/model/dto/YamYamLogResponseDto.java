package com.ssafy.yamyam.model.dto;


import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ssafy.yamyam.model.entity.Category;
import com.ssafy.yamyam.model.entity.YamYamLog;
import com.ssafy.yamyam.model.entity.YamYamLog.MealType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class YamYamLogResponseDto {

    private Long logId;
    private Long foodId;
    private LocalDate mealDate;
    private MealType mealType;

    private String foodName;
    private Category category;

    private double servingSize;
    private double actualEnergy;
    private double actualProtein;
    private double actualFat;
    private double actualCarbs;

    private LocalDateTime createdAt;

    public static YamYamLogResponseDto from(YamYamLog log) {
        return YamYamLogResponseDto.builder()
                .logId(log.getLogId())
                .foodId(log.getFood().getFoodId())
                .mealDate(log.getMealDate())
                .mealType(log.getMealType())
                .foodName(log.getFood().getFoodName())
                .category(log.getFood().getCategory())
                .servingSize(log.getServingSize())
                .actualEnergy(log.getActualEnergy())
                .actualProtein(log.getActualProtein())
                .actualFat(log.getActualFat())
                .actualCarbs(log.getActualCarbs())
                .createdAt(log.getCreatedAt())
                .build();
    }
}