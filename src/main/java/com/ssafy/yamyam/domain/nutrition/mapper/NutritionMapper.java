package com.ssafy.yamyam.domain.nutrition.mapper;

import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NutritionMapper {

    // nutrition_analysis 테이블
    void insertNutritionAnalysis(NutritionAnalysis analysis);
    NutritionAnalysis findByVideoId(@Param("videoId") Long videoId);
    void updateNutritionAnalysis(NutritionAnalysis analysis);

    // recognized_food_item 테이블
    void deleteByVideoId(@Param("videoId") Long videoId);
    void resetToRetry(@Param("videoId") Long videoId);

    void insertFoodItem(RecognizedFoodItem item);
    List<RecognizedFoodItem> findFoodItemsByAnalysisId(@Param("nutritionAnalysisId") Long nutritionAnalysisId);

 // 기존 내용 유지 후 아래 메서드 추가
    @org.apache.ibatis.annotations.Delete("DELETE FROM RECOGNIZED_FOOD_ITEM WHERE nutrition_analysis_id = #{analysisId}")
    void deleteFoodItemsByAnalysisId(@org.apache.ibatis.annotations.Param("analysisId") Long analysisId);
	
    
    Map<String, Object> findDailySummary(Long userId, String date);
	
    
    Object findDailyLogs(Long userId, String date);
}
