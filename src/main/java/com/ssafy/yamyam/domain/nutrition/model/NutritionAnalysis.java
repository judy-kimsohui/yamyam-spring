package com.ssafy.yamyam.domain.nutrition.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NutritionAnalysis {

    private Long id;

    // video 테이블 FK
    private Long videoId;

    // 분석 상태
    private Status status;

    // 총 영양 합계 (음식 전체)
    private Double totalCalories;
    private Double totalCarbs;
    private Double totalProtein;
    private Double totalFat;

    // Claude가 인식한 음식 목록 원본 JSON (파싱 전 보관)
    private String foodsJson;

    // 실패 시 에러 메시지
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, DONE, FAILED
    }
}
