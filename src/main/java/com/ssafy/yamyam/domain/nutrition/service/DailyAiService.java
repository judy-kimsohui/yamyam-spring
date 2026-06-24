package com.ssafy.yamyam.domain.nutrition.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyAiService {

    private final NutritionMapper nutritionMapper;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.base-url:https://gms.ssafy.io/gmsapi}")
    private String gmsBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String gmsApiKey;

    @Transactional
    public String evaluateDailyLog(Long userId, String date) {
        // 1. 데이터 준비
        User user = userService.getUserInfo(userId);
        Map<String, Object> summary = nutritionMapper.findDailySummary(userId, date);

        if (user == null || summary == null || summary.isEmpty()) {
            throw new IllegalArgumentException("평가할 유저 정보 또는 식단 기록이 없습니다.");
        }

        // 2. 프롬프트 구성 (JSON 형식 강제)
        String prompt = String.format("""
            당신은 전문 영양사입니다. 사용자의 오늘 하루 총 식단 섭취량을 분석하여 JSON 형식으로만 답변하세요.
            다른 텍스트는 절대 포함하지 마세요.
            {
                "summary": "전체적인 평가 한 줄",
                "advice": "내일 식단을 위한 조언",
                "missingNutrients": ["부족한 영양소1", "부족한 영양소2"],
                "score": 85
            }
            [사용자 정보] 성별: %s, 나이: %d, 키: %.1f, 몸무게: %.1f, 목표: %s
            [오늘 섭취] 칼로리: %.0f kcal, 탄수화물: %.0f g, 단백질: %.0f g, 지방: %.0f g
            """,
                user.getGender(), user.getAge(), user.getHeight(), user.getWeight(),
                (user.getUserGoal() != null ? user.getUserGoal() : "건강 관리"),
                toDouble(summary.get("total_calories")), toDouble(summary.get("total_carbs")),
                toDouble(summary.get("total_protein")), toDouble(summary.get("total_fat"))
        );

        // 3. API 호출 로직 (VideoAnalysisService와 동일한 방식 사용)
        Map<String, Object> messagePayload = new LinkedHashMap<>();
        messagePayload.put("role", "user");
        messagePayload.put("content", prompt);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", List.of(messagePayload));
        requestBody.put("temperature", 0.7);

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
                .block();

        // 4. 응답 파싱 및 마크다운 정제
        List<?> choices = (List<?>) responseBody.get("choices");
        Map<?, ?> resMessage = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        String aiResponse = (String) resMessage.get("content");

        if (aiResponse.startsWith("```")) {
            aiResponse = aiResponse.replaceAll("^```json\\n?", "").replaceAll("```$", "").trim();
        }

        // 5. DB 저장
        nutritionMapper.upsertDailyAiComment(userId, date, aiResponse);

        return aiResponse;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return 0.0; }
    }
}