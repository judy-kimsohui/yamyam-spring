package com.ssafy.yamyam.domain.nutrition.service;

import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.user.model.User;
import com.ssafy.yamyam.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyAiService {

    private final NutritionMapper nutritionMapper;
    private final UserService userService;

    @Value("${spring.ai.openai.base-url:https://gms.ssafy.io/gmsapi}")
    private String gmsBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String gmsApiKey;

    @Transactional
    public String evaluateDailyLog(Long userId, String date) {
        User user = userService.getUserInfo(userId);
        Map<String, Object> summary = nutritionMapper.findDailySummary(userId, date);

        if (user == null || summary == null || summary.isEmpty()) {
            throw new IllegalArgumentException("평가할 사용자 정보 또는 식단 기록이 없습니다.");
        }

        double totalCalories = toDouble(summary.get("totalCalories"));
        double totalCarbs = toDouble(summary.get("totalCarbs"));
        double totalProtein = toDouble(summary.get("totalProtein"));
        double totalFat = toDouble(summary.get("totalFat"));

        if (totalCalories <= 0 && totalCarbs <= 0 && totalProtein <= 0 && totalFat <= 0) {
            throw new IllegalArgumentException("분석 완료된 식단 영양 정보가 없습니다.");
        }

        String prompt = String.format("""
            당신은 식단 영양 코치입니다.
            아래 사용자의 하루 식사 합계를 보고 한국어로 2~3문장의 짧은 피드백만 작성하세요.
            JSON, 마크다운, 코드블록, 점수표는 쓰지 마세요.
            칼로리/탄수화물/단백질/지방 중 눈에 띄는 균형을 언급하고, 다음 식사에서 바로 실천할 조언을 하나 포함하세요.

            [사용자 정보] 성별: %s, 나이: %d, 키: %.1fcm, 몸무게: %.1fkg, 목표: %s
            [하루 식사 합계] 칼로리 %.0f kcal, 탄수화물 %.0f g, 단백질 %.0f g, 지방 %.0f g
            """,
            user.getGender(),
            user.getAge(),
            user.getHeight(),
            user.getWeight(),
            user.getUserGoal() != null ? user.getUserGoal() : "건강 관리",
            totalCalories,
            totalCarbs,
            totalProtein,
            totalFat
        );

        Map<String, Object> messagePayload = new LinkedHashMap<>();
        messagePayload.put("role", "user");
        messagePayload.put("content", prompt);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(messagePayload));
        requestBody.put("temperature", 0.4);

        WebClient webClient = WebClient.builder()
            .baseUrl(gmsBaseUrl)
            .defaultHeader("Authorization", "Bearer " + gmsApiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();

        Map<String, Object> responseBody = webClient.post()
            .uri("/v1/chat/completions")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .block(Duration.ofSeconds(60));

        String aiResponse = extractMessage(responseBody);
        try {
            nutritionMapper.upsertDailyAiComment(userId, date, aiResponse);
        } catch (Exception e) {
            log.warn("일일 AI 피드백 저장 실패. 응답은 그대로 반환합니다. userId={}, date={}, message={}",
                userId, date, e.getMessage());
        }
        return aiResponse;
    }

    public String getDailyAiComment(Long userId, String date) {
        try {
            return nutritionMapper.findDailyAiComment(userId, date);
        } catch (Exception e) {
            log.warn("저장된 일일 AI 피드백 조회 실패. userId={}, date={}, message={}",
                userId, date, e.getMessage());
            return null;
        }
    }

    private String extractMessage(Map<String, Object> responseBody) {
        List<?> choices = (List<?>) responseBody.get("choices");
        Map<?, ?> resMessage = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        String content = (String) resMessage.get("content");
        if (content == null) {
            return "AI 피드백을 생성하지 못했습니다.";
        }

        content = content.trim();
        if (content.startsWith("```")) {
            content = content.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").trim();
        }
        return content;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
