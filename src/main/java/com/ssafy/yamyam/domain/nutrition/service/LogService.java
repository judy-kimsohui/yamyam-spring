package com.ssafy.yamyam.domain.nutrition.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {
    private final NutritionMapper nutritionMapper;

    public Map<String, Object> getDailyLogData(Long userId, String date) {
        Map<String, Object> result = new HashMap<>();
        
        // 1. 요약 정보 가져오기
        Map<String, Object> summary = nutritionMapper.findDailySummary(userId, date);
        result.put("total", summary != null ? summary : new HashMap<>());
        
        // 2. 상세 리스트 가져오기
        result.put("logs", nutritionMapper.findDailyLogs(userId, date));
        
        return result;
    }
}