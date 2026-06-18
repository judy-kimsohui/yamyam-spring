package com.ssafy.yamyam.domain.nutrition.service;

import com.ssafy.yamyam.domain.nutrition.dto.NutritionResponseDto;
import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionService {

    private final NutritionMapper nutritionMapper;

    public NutritionResponseDto getNutritionByVideoId(Long videoId) {
        NutritionAnalysis analysis = nutritionMapper.findByVideoId(videoId);

        if (analysis == null) {
            return NutritionResponseDto.pending(videoId);
        }

        NutritionResponseDto dto = new NutritionResponseDto();
        dto.setVideoId(videoId);
        dto.setStatus(analysis.getStatus().name());

        if (analysis.getStatus() == NutritionAnalysis.Status.DONE) {
            dto.setTotalCalories(analysis.getTotalCalories());
            dto.setTotalCarbs(analysis.getTotalCarbs());
            dto.setTotalProtein(analysis.getTotalProtein());
            dto.setTotalFat(analysis.getTotalFat());

            List<RecognizedFoodItem> items =
                    nutritionMapper.findFoodItemsByAnalysisId(analysis.getId());

            dto.setFoods(items.stream().map(item -> {
                NutritionResponseDto.FoodItemDto foodDto = new NutritionResponseDto.FoodItemDto();
                foodDto.setFoodName(item.getFoodName());
                foodDto.setCalories(item.getCalories());
                foodDto.setCarbs(item.getCarbs());
                foodDto.setProtein(item.getProtein());
                foodDto.setFat(item.getFat());
                return foodDto;
            }).collect(Collectors.toList()));
        }

        if (analysis.getStatus() == NutritionAnalysis.Status.FAILED) {
            dto.setErrorMessage(analysis.getErrorMessage());
        }

        return dto;
    }
}
