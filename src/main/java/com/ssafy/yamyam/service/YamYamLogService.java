package com.ssafy.yamyam.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.yamyam.model.dto.FoodResponseDto;
import com.ssafy.yamyam.model.dto.YamYamLogRequestDto;
import com.ssafy.yamyam.model.dto.YamYamLogResponseDto;
import com.ssafy.yamyam.repository.CategoryRepository;
import com.ssafy.yamyam.repository.FoodRepository;
import com.ssafy.yamyam.repository.YamYamLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class YamYamLogService {
    private final YamYamLogRepository yamYamLogRepository;
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    
    // 식단 기록
    public YamYamLogResponseDto save(Long userId, YamYamLogRequestDto dto) { 
    	return null;
    }
    // 하루 조회
    public List<YamYamLogResponseDto> getDaily(Long userId, LocalDate date) {  
    	return null;
    }
    // 일주일 조회
    public List<YamYamLogResponseDto> getWeekly(Long userId, LocalDate start) {  
    	return null;
    }
    // 삭제
    public void delete(Long logId) {  
    	
    }
    
    
    // 대분류 목록 조회
    public List<String> getMainCategories() {
    	return null;
    }

    // 소분류 목록 조회
    public List<String> getSubCategories(String mainCategory) {
    	return null;
    }

    // 카테고리로 음식 목록 조회
    public List<FoodResponseDto> getFoodsByCategory(String mainCategory, String subCategory) {
    	return null;
    }

    // 음식명 검색
    public List<FoodResponseDto> searchFood(String keyword) {
    	return null;
    }
}