package com.ssafy.yamyam.domain.video.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class VideoAnalysisService {

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<AnalysisResult> analyze(String description, String mealType) {
        if (apiKey == null || apiKey.isBlank()) return Optional.empty();

        String prompt = String.format(
                "식단 기록입니다.\n식사 종류: %s\n음식 설명: %s\n\n" +
                "이 식단의 예상 영양정보를 추정하고 짧은 한국어 코멘트를 작성하세요.\n" +
                "반드시 아래 JSON 형식으로만 응답하세요 (다른 텍스트 없이):\n" +
                "{\"calories\":숫자,\"carbs\":숫자,\"protein\":숫자,\"fat\":숫자,\"comment\":\"한 문장\"}",
                mealType, description != null ? description : "설명 없음"
        );

        try {
            Map<?, ?> response = webClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .bodyValue(Map.of(
                            "model", "claude-haiku-4-5-20251001",
                            "max_tokens", 256,
                            "messages", List.of(Map.of("role", "user", "content", prompt))
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(20));

            List<?> content = (List<?>) response.get("content");
            String text = (String) ((Map<?, ?>) content.get(0)).get("text");
            AnalysisResult result = objectMapper.readValue(text.trim(), AnalysisResult.class);
            return Optional.of(result);
        } catch (Exception e) {
            log.warn("AI 분석 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Data
    public static class AnalysisResult {
        private Double calories;
        private Double carbs;
        private Double protein;
        private Double fat;
        private String comment;
    }
}
