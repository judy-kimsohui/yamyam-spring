package com.ssafy.yamyam.domain.nutrition.mapper;

import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NutritionMapper {

    // nutrition_analysis 테이블
    void insertNutritionAnalysis(NutritionAnalysis analysis);
    NutritionAnalysis findByVideoId(@Param("videoId") Long videoId);
    void updateNutritionAnalysis(NutritionAnalysis analysis);

    // recognized_food_item 테이블
    void insertFoodItem(RecognizedFoodItem item);
    List<RecognizedFoodItem> findFoodItemsByAnalysisId(@Param("nutritionAnalysisId") Long nutritionAnalysisId);
}
