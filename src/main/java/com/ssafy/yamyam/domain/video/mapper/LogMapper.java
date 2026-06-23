package com.ssafy.yamyam.domain.video.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yamyam.domain.video.dto.DailyDietSummaryDto;
import com.ssafy.yamyam.domain.video.dto.PeriodNutrientTrendDto;

@Mapper
public interface LogMapper {
	
	
	// 특정 날짜의 탄단지/칼로리 합산 조회
    DailyDietSummaryDto findDailyNutrientSum(
            @Param("userId") Long userId, 
            @Param("targetDate") String targetDate
    );

    // 특정 날짜의 먹은 음식 이름 목록 조회
    List<String> findDailyFoodNames(
            @Param("userId") Long userId, 
            @Param("targetDate") String targetDate
    );

    // 특정 날짜의 기록된 몸무게 조회
    Double findDailyWeight(
            @Param("userId") Long userId, 
            @Param("targetDate") String targetDate
    );
    
    /**
     * ④ 새벽에 완성된 AI 피드백 리포트를 DB에 영구 저장합니다.
     * (ON DUPLICATE KEY UPDATE 문을 써서 이미 존재하면 수정되도록 설계할 예정)
     */
    void upsertDailyReport(
            @Param("userId") Long userId, 
            @Param("reportDate") String reportDate, 
            @Param("feedback") String feedback
    );

    /**
     * ⑤ 마이로그 진입 시 해당 날짜에 미리 구워둔 AI 피드백이 있는지 단건 조회합니다.
     */
    String findAiFeedback(
            @Param("userId") Long userId, 
            @Param("targetDate") String targetDate
    );
    
    List<PeriodNutrientTrendDto> findNutrientTrendByPeriod(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
