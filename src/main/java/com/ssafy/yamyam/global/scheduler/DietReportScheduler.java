package com.ssafy.yamyam.global.scheduler;

import com.ssafy.yamyam.domain.video.dto.DailyDietSummaryDto;
import com.ssafy.yamyam.domain.video.mapper.LogMapper;
import com.ssafy.yamyam.domain.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DietReportScheduler {

    private final VideoService videoService;
    private final LogMapper logMapper;
    // TODO: 프로젝트에 실제 등록된 AI(OpenAI 또는 GMS) 서비스 컴포넌트를 DI 받으세요.
    // private final OpenAiService openAiService; 

    /**
     * [새벽 정기 배치] 매일 새벽 2시에 자동으로 실행되어 어제 하루 식단 총평 AI 리포트를 굽습니다.
     * 크론 표현식: 초 분 시 일 월 요일 (0 0 2 * * *)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void generateYesterdayAiReports() {
        String yesterday = LocalDate.now().minusDays(1).toString();
        log.info("=== [배치 엔진] 어제({}) 하루 식단 AI 리포트 생성 프로세스 가동 ===", yesterday);

        // 1. (가정) 어제 날짜로 식단을 1개 이상 업로드한 활성 유저 ID 목록을 추출합니다.
        // * 팁: 설계가 복잡하다면 전체 유저 목록을 돌려도 되지만, 성능을 위해 식단을 쓴 유저만 추리는 것을 권장합니다.
        // List<Long> activeUserIds = logMapper.findActiveUserIdsByDate(yesterday);
        
        // 테스트용 계정 배열 예시 (실제 구현 시 유저 루프 처리)
        List<Long> activeUserIds = List.of(1L); 

        for (Long userId : activeUserIds) {
            try {
                // 2. 우리가 만든 만능 수집기 가동 -> 어제 하루 총 영양소, 음식 이름들, 체중 수집
                DailyDietSummaryDto summary = videoService.getDailyDietSummary(userId, yesterday);

                // 어제 먹은 칼로리가 아예 없다면 굳이 AI 비용을 들여 리포트를 쓰지 않고 패스합니다.
                if (summary.getTotalCalories() == 0) {
                    continue;
                }

                // 3. AI에게 보낼 정밀 프롬프트 조립
                String prompt = createReportPrompt(summary);

                // 4. AI 서비스 호출 및 텍스트 추출 (프로젝트의 OpenAI 호출 로직으로 연동 필요)
                String aiFeedbackText = "어제 섭취하신 단백질 65g은 회원님의 근손실 방지에 최적의 수치입니다. 다만 제육덮밥으로 인해 나트륨과 탄수화물 비중이 높으니, 오늘은 맑은 소고기무국이나 샐러드 위주의 정갈한 식단을 짝지어 보세요!";
                // aiFeedbackText = openAiService.askGpt(prompt); 

                // 5. DB에 안전하게 저장 또는 최신화 (UPSERT)
                logMapper.upsertDailyReport(userId, yesterday, aiFeedbackText);
                log.info("=> 유저 {} 번의 어제 자 AI 리포트 굽기 완료.", userId);

            } catch (Exception e) {
                log.error("❌ 유저 {} 번의 AI 리포트 생성 중 예외 발생: ", userId, e);
            }
        }
        log.info("=== [배치 엔진] AI 리포트 예약 작업 종료 ===");
    }

    /**
     * AI 가 데이터 인과관계를 완벽히 인지하도록 정량적/정성적 데이터를 주입하는 프롬프트 빌더
     */
    private String createReportPrompt(DailyDietSummaryDto summary) {
        return String.format(
            "너는 스포츠 영양학 및 헬스케어 전문 AI 트레이너야. 아래의 유저 하루 식단 데이터를 분석해서 '어제 하루 총평'을 다정한 말투로 딱 3줄 요약해서 조언해줘.\n\n" +
            "- 하루 총 섭취 칼로리: %.1f kcal\n" +
            "- 탄수화물: %.1f g, 단백질: %.1f g, 지방: %.1f g\n" +
            "- 먹은 음식 리스트: %s\n" +
            "- 기록된 당일 체중: %s kg\n\n" +
            "형식은 마크다운 없이 순수 텍스트 문자열로만 응답해줘.",
            summary.getTotalCalories(),
            summary.getTotalCarbs(),
            summary.getTotalProtein(),
            summary.getTotalFat(),
            String.join(", ", summary.getFoodNames()),
            summary.getRecordedWeight() != null ? summary.getRecordedWeight().toString() : "미기록"
        );
    }
}