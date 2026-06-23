package com.ssafy.yamyam.domain.video.dto;

import java.util.List;

import lombok.Data;



//  먹은 총량, 음식 목록, 체중 한데 모을 수 있는 DTO
@Data
public class DailyDietSummaryDto {
	
	private double totalCalories;
	private double totalCarbs;
	private double totalProtein;
	private double totalFat;
	
	private List<String> foodNames;
	
	private Double recordedWeight;
}
