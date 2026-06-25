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
	
    
    Map<String, Object> findDailySummary(@Param("userId") Long userId, @Param("date") String date);
	
    
    Object findDailyLogs(@Param("userId") Long userId, @Param("date") String date);

    List<Map<String, Object>> findDailyNutritionTrend(@Param("userId") Long userId,
                                                      @Param("startDate") String startDate,
                                                      @Param("endDate") String endDate);

    String findDailyAiComment(@Param("userId") Long userId, @Param("recordDate") String recordDate);

    List<Map<String, Object>> findMonthlyDayData(@Param("userId") Long userId,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    // 일일 종합 코멘트 저장 (UPSERT)
    void upsertDailyAiComment(@Param("userId") Long userId,
                              @Param("recordDate") String recordDate,
                              @Param("aiComment") String aiComment);
}
