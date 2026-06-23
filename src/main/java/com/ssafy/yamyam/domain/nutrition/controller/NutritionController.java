package com.ssafy.yamyam.domain.nutrition.controller;

import com.ssafy.yamyam.domain.nutrition.dto.NutritionResponseDto;
import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.service.NutritionService;
import com.ssafy.yamyam.domain.nutrition.service.VideoAnalysisService;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;
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
        if (existing != null && existing.getStatus() != NutritionAnalysis.Status.FAILED) {
            return ResponseEntity.noContent().build();
        }
        VideoDto video = videoMapper.findById(videoId, 0L);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }
        if (existing != null) {
            nutritionMapper.deleteByVideoId(videoId);
        }
        videoAnalysisService.analyzeAsync(videoId, video.getVideoUrl());
        return ResponseEntity.accepted().build();
    }
}
