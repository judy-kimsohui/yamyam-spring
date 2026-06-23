package com.ssafy.yamyam.domain.video.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class DailyDietReport {
    private Long id;
    private Long userId;
    private LocalDate reportDate;       // 리포트 대상 날짜 (예: 어제 날짜)
    private String aiDailyFeedback;     // AI가 최종 생성한 3줄 요약 총평 텍스트
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}