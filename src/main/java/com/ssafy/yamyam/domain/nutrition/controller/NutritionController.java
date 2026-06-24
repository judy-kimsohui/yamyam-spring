package com.ssafy.yamyam.domain.nutrition.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yamyam.domain.nutrition.dto.NutritionResponseDto;
import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.service.NutritionService;
import com.ssafy.yamyam.domain.nutrition.service.VideoAnalysisService;
import com.ssafy.yamyam.domain.video.dto.FoodItemRequestDto;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import com.ssafy.yamyam.domain.video.service.VideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;
    private final VideoService videoService; // 비즈니스 수정 로직 조율용 주입
    private final NutritionMapper nutritionMapper;
    private final VideoAnalysisService videoAnalysisService;
    private final VideoMapper videoMapper;

    /**
     * GET /api/videos/{videoId}/nutrition
     * 영양 분석 결과 조회
     */
    @GetMapping("/{videoId}/nutrition")
    public ResponseEntity<NutritionResponseDto> getNutrition(@PathVariable Long videoId) {
        NutritionResponseDto result = nutritionService.getNutritionByVideoId(videoId);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/videos/{videoId}/analyze
     * AI 영양 분석 트리거 (스크립트/재시도용)
     * - 이미 DONE/PENDING 이면 204 반환
     * - FAILED 이면 기존 레코드 삭제 후 재분석
     * - 분석 없으면 신규 분석 시작, 202 반환
     */
    @PostMapping("/{videoId}/analyze")
    public ResponseEntity<Void> triggerAnalysis(@PathVariable Long videoId) {
        NutritionAnalysis existing = nutritionMapper.findByVideoId(videoId);
        if (existing != null) {
            if (existing.getStatus() == NutritionAnalysis.Status.PENDING
                    || existing.getStatus() == NutritionAnalysis.Status.DONE) {
                return ResponseEntity.noContent().build();
            }
            // FAILED: 3회 초과 시 포기
            int retryCount = existing.getRetryCount() != null ? existing.getRetryCount() : 0;
            if (retryCount >= 3) {
                return ResponseEntity.status(429).build();
            }
            nutritionMapper.resetToRetry(videoId); // PENDING으로 되돌리고 retry_count++
        }
        VideoDto video = videoMapper.findById(videoId, 0L);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        videoAnalysisService.analyzeAsync(videoId, video.getVideoUrl());
        return ResponseEntity.accepted().build();
    }
    
    
    

    /**
     * 🌟 POST /api/videos/{videoId}/nutrition/items
     * 사용자가 AI 오인식 결과에 새로운 음식을 강제 추가
     */
    @PostMapping("/{videoId}/nutrition/items")
    public ResponseEntity<String> addFoodItem(
            @PathVariable Long videoId, 
            @RequestBody FoodItemRequestDto dto) {
        try {
            videoService.addManualFoodItem(videoId, dto);
            return ResponseEntity.ok("음식 항목이 수동으로 정상 추가되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 🌟 PUT /api/videos/{videoId}/nutrition/items
     * 사용자가 오인식된 음식을 직접 수정 (이름 변경 또는 영양소 직접 보정)
     */
    @PutMapping("/{videoId}/nutrition/items")
    public ResponseEntity<String> updateFoodItem(
            @PathVariable Long videoId, 
            @RequestBody FoodItemRequestDto dto) {
    	
    	
        try {
            videoService.updateFoodItem(videoId, dto);
            return ResponseEntity.ok("음식 정보가 성공적으로 수정 및 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{videoId}/nutrition/items/batch")
    public ResponseEntity<String> updateAllFoodItems(
            @PathVariable Long videoId,
            @RequestBody List<FoodItemRequestDto> dtoList,
            HttpServletRequest request) {
        
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        // 서비스 단에서 루프를 돌며 자식 테이블 데이터를 한 번에 밀어넣고 
        // 최종적으로 recalculateTotalNutrition(videoId)를 딱 '한 번'만 수행합니다.
        videoService.updateBatchFoodItems(videoId, dtoList, loginUserId);
        
        return ResponseEntity.ok("식단 정보가 일괄 저장되었습니다.");
    }

    /**
     * 🌟 DELETE /api/videos/{videoId}/nutrition/items/{foodItemId}
     * 잘못 인식된 개별 음식을 제거
     */
    @DeleteMapping("/{videoId}/nutrition/items/{foodItemId}")
    public ResponseEntity<String> deleteFoodItem(
            @PathVariable Long videoId, 
            @PathVariable Long foodItemId) {
        try {
            videoService.deleteFoodItem(videoId, foodItemId);
            return ResponseEntity.ok("해당 음식 정보가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
