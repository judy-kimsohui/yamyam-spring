package com.ssafy.yamyam.domain.video.dto;

import java.util.List;
import lombok.Data;

@Data
public class DailyDashboardResponseDto {
    // 1. 유저의 목표 가이드라인선
    private NutrientGoalDto goal;

    // 2. 오늘 현재까지 실시간 완료된(DONE) 누적 데이터
    private DailyDietSummaryDto summary;

    // 3. 목표 대비 현재 상태 (부족량/초과량) -> 프론트엔드가 뺄셈하지 않게 백엔드가 연산
    private double remainCalories; // + 이면 남은 양, - 이면 초과량
    private double remainCarbs;
    private double remainProtein;
    private double remainFat;

    // 4. 수치 기반 즉석 실시간 규칙 기반 피드백 메시지
    private String ruleFeedback;
    private String aiDailyFeedback;    // 🅱️ [신설] 새벽 배치로 구워둔 어제 자 정기 AI 피드백 총평
}