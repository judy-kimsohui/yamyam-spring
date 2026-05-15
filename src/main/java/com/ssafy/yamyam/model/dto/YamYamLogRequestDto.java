package com.ssafy.yamyam.model.dto;

import java.time.LocalDate;

import com.ssafy.yamyam.model.entity.YamYamLog.MealType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YamYamLogRequestDto {
    private LocalDate mealDate;
    private MealType mealType;
    private Long foodId;
    private double servingSize;
}