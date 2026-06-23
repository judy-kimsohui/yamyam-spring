package com.ssafy.yamyam.domain.video.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class PeriodNutrientTrendDto {
    private LocalDate date;         
    private double dailyCalories;   
    private double dailyCarbs;      
    private double dailyProtein;   
    private double dailyFat;   
}