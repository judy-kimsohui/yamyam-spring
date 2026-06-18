package com.ssafy.yamyam.domain.nutrition.controller;

import com.ssafy.yamyam.domain.nutrition.dto.NutritionResponseDto;
import com.ssafy.yamyam.domain.nutrition.service.NutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    /**
     * GET /api/videos/{videoId}/nutrition
     * 영양 분석 결과 조회
     * - PENDING: 분석 중
     * - DONE: 영양 데이터 포함
     * - FAILED: 에러 메시지 포함
     */
    @GetMapping("/{videoId}/nutrition")
    public ResponseEntity<NutritionResponseDto> getNutrition(@PathVariable Long videoId) {
        NutritionResponseDto result = nutritionService.getNutritionByVideoId(videoId);
        return ResponseEntity.ok(result);
    }
}
