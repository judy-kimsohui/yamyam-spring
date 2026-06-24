package com.ssafy.yamyam.domain.nutrition.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yamyam.domain.nutrition.dto.NutritionResponseDto;
import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionService {

    private final NutritionMapper nutritionMapper;
    private final VideoMapper videoMapper;

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
                foodDto.setQuantity(item.getQuantity()); 
                return foodDto;
            }).collect(Collectors.toList()));
        }

        if (analysis.getStatus() == NutritionAnalysis.Status.FAILED) {
            dto.setErrorMessage(analysis.getErrorMessage());
        }

        return dto;
    }
    @Transactional(readOnly = false)
    public void updateNutrition(Long videoId, List<RecognizedFoodItem> newFoods) {
        NutritionAnalysis analysis = nutritionMapper.findByVideoId(videoId);
        if (analysis == null) {
            throw new IllegalArgumentException("영양 분석 결과가 존재하지 않습니다.");
        }

        // 1. 자식 데이터(음식 항목) 전체 삭제
        nutritionMapper.deleteFoodItemsByAnalysisId(analysis.getId());

        double totalCalories = 0, totalCarbs = 0, totalProtein = 0, totalFat = 0;

        for (RecognizedFoodItem item : newFoods) {
            // 수량이 없거나 0이면 기본 1인분 처리
            double q = (item.getQuantity() == null || item.getQuantity() <= 0) ? 1.0 : item.getQuantity();
            item.setQuantity(q);

            // [중요] 1인분 영양소 계산 (데이터 정규화)
            // 만약 프론트에서 넘어온 item.getCalor	ies()가 이미 q가 곱해진 값이라면 
            // 여기서 1인분 값으로 복원하여 DB에 저장해야 나중에 다시 불러올 때 문제가 없습니다.
            // 현재는 프론트가 1인분 스펙을 보낸다고 가정하고 처리합니다.
            double caloriesPerServing = (item.getCalories() != null) ? item.getCalories() : 0;
            double carbsPerServing = (item.getCarbs() != null) ? item.getCarbs() : 0;
            double proteinPerServing = (item.getProtein() != null) ? item.getProtein() : 0;
            double fatPerServing = (item.getFat() != null) ? item.getFat() : 0;

            // DB 저장을 위해 객체 상태 업데이트
            item.setCalories(caloriesPerServing);
            item.setCarbs(carbsPerServing);
            item.setProtein(proteinPerServing);
            item.setFat(fatPerServing);
            item.setNutritionAnalysisId(analysis.getId());
            
            // 2. DB에 1인분 기준 데이터 삽입
            nutritionMapper.insertFoodItem(item);

            // 3. 총합 계산 (총합은 여기서만 계산하여 분석 결과에 반영)
            totalCalories += caloriesPerServing * q;
            totalCarbs += carbsPerServing * q;
            totalProtein += proteinPerServing * q;
            totalFat += fatPerServing * q;
        }

        // 4. 분석 결과 테이블(부모)의 총합 정보 갱신
        analysis.setTotalCalories(totalCalories);
        analysis.setTotalCarbs(totalCarbs);
        analysis.setTotalProtein(totalProtein);
        analysis.setTotalFat(totalFat);
        analysis.setFoodsJson("[]"); 
        nutritionMapper.updateNutritionAnalysis(analysis);
    }
}
