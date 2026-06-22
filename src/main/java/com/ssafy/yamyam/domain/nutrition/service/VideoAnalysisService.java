package com.ssafy.yamyam.domain.nutrition.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoAnalysisService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.ai.openai.base-url:https://gms.ssafy.io/gmsapi}")
    private String gmsBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String gmsApiKey;

    private final S3Client s3Client;
    private final NutritionMapper nutritionMapper;
    private final VideoFrameExtractor frameExtractor;
    private final ObjectMapper objectMapper;

    private static final String ANALYSIS_PROMPT = """
            이 음식 사진들을 종합적으로 분석해서 아래 JSON 형식으로만 응답해줘. 다른 텍스트는 절대 포함하지 마.
            음식이 보이지 않으면 foods를 빈 배열로 반환해.
            수치를 10 단위로 뭉뚱그려 응답하지 말고, 영상 속 음식의 실제 대략적인 양과 부피를 고려하여 1의 자리 단위까지 정밀하게 추정해줘 (예: 523, 42.5 등).         
            {
              "foods": [
                {
                  "foodName": "음식 이름 (한국어)",
                  "calories": 숫자,
                  "carbs": 숫자,
                  "protein": 숫자,
                  "fat": 숫자
                }
              ]
            }
            """;

    @Async("analysisExecutor")
    public void analyzeAsync(Long videoId, String storedVideoPath) {
        if (storedVideoPath == null) {
            log.error("[영양분석] videoId={} 비디오 경로가 널(null)입니다.", videoId);
            return;
        }

        NutritionAnalysis analysis = new NutritionAnalysis();
        analysis.setVideoId(videoId);
        analysis.setStatus(NutritionAnalysis.Status.PENDING);
        nutritionMapper.insertNutritionAnalysis(analysis);

        File tempVideoFile = null;

        try {
            String bucketName = this.bucket;
            String s3Key = storedVideoPath;

            // 🌟 1. 들어온 인자값 형태에 따른 S3 Key / Bucket 정밀 추출 방어 로직
            if (storedVideoPath.startsWith("s3://")) {
                java.net.URI uri = new java.net.URI(storedVideoPath);
                bucketName = uri.getHost();
                String path = uri.getPath();
                s3Key = path.startsWith("/") ? path.substring(1) : path;
            } else if (storedVideoPath.startsWith("http://") || storedVideoPath.startsWith("https://")) {
                java.net.URI uri = new java.net.URI(storedVideoPath);
                bucketName = uri.getHost().split("\\.")[0];
                s3Key = uri.getPath().substring(1);
            }

            log.info("[영양분석] videoId={} S3 자원 로컬 /tmp 캐싱 시작 (Bucket: {}, Key: {})", videoId, bucketName, s3Key);

            // 🌟 2. /tmp 경로에 고유 임시 파일 버퍼 생성 및 다운로드
            tempVideoFile = File.createTempFile("s3_video_" + videoId + "_", ".mp4");
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.getObject(getObjectRequest, Paths.get(tempVideoFile.getAbsolutePath()));
            String targetLocalPath = tempVideoFile.getAbsolutePath();

            log.info("[영양분석] videoId={} 로컬 다운로드 성공 -> 프레임 추출 시작", videoId);
            List<byte[]> frames = frameExtractor.extractFrames(targetLocalPath);

            if (frames == null || frames.isEmpty()) {
                throw new RuntimeException("비디오 파일에서 유효한 이미지 프레임을 추출하지 못했습니다.");
            }

            // GMS / OpenAI 표준 멀티모달 컨텐츠 리스트 조립
            List<Map<String, Object>> contentList = new ArrayList<>();

            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("type", "text");
            textPart.put("text", ANALYSIS_PROMPT);
            contentList.add(textPart);

            for (byte[] frameBytes : frames) {
                String base64Image = Base64.getEncoder().encodeToString(frameBytes);

                Map<String, Object> imageUrlMap = new LinkedHashMap<>();
                imageUrlMap.put("url", "data:image/jpeg;base64," + base64Image.trim());

                Map<String, Object> imagePart = new LinkedHashMap<>();
                imagePart.put("type", "image_url");
                imagePart.put("image_url", imageUrlMap);
                contentList.add(imagePart);
            }

            Map<String, Object> messagePayload = new LinkedHashMap<>();
            messagePayload.put("role", "user");
            messagePayload.put("content", contentList);

            // GMS 게이트웨이 라우팅 보장을 위한 LinkedHashMap 모델 선두 직렬화 고정
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("messages", List.of(messagePayload));
            requestBody.put("temperature", 0.2);

            log.info("[영양분석] videoId={} GMS AI 분석 요청 전송", videoId);

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
                    .block(java.time.Duration.ofSeconds(60));

            List<?> choices = (List<?>) responseBody.get("choices");
            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> resMessage = (Map<?, ?>) firstChoice.get("message");
            String response = (String) resMessage.get("content");

            if (response != null) {
                response = response.trim();
                if (response.startsWith("```")) {
                    response = response.replaceAll("^```[a-z]*\\n?", "").replaceAll("```$", "").trim();
                }
            }

            log.info("[영양분석] videoId={} GPT-4o 분석 응답 수신 완료", videoId);

            List<RecognizedFoodItem> foodItems = parseAnalysisResult(response, analysis.getId());

            double totalCalories = foodItems.stream().mapToDouble(f -> nullSafe(f.getCalories())).sum();
            double totalCarbs    = foodItems.stream().mapToDouble(f -> nullSafe(f.getCarbs())).sum();
            double totalProtein  = foodItems.stream().mapToDouble(f -> nullSafe(f.getProtein())).sum();
            double totalFat      = foodItems.stream().mapToDouble(f -> nullSafe(f.getFat())).sum();

            analysis.setStatus(NutritionAnalysis.Status.DONE);
            analysis.setTotalCalories(totalCalories);
            analysis.setTotalCarbs(totalCarbs);
            analysis.setTotalProtein(totalProtein);
            analysis.setTotalFat(totalFat);
            analysis.setFoodsJson(response);
            nutritionMapper.updateNutritionAnalysis(analysis);

            for (RecognizedFoodItem item : foodItems) {
                item.setNutritionAnalysisId(analysis.getId());
                nutritionMapper.insertFoodItem(item);
            }

            log.info("[영양분석] videoId={} 분석 최종 기록 마감 완료 — {}kcal, 음식 {}개",
                    videoId, totalCalories, foodItems.size());

        } catch (WebClientResponseException e) {
            String responseBodyStr = e.getResponseBodyAsString();
            log.error("[영양분석] videoId={} GMS API 오류 응답 status={} body={}",
                    videoId, e.getStatusCode(), responseBodyStr, e);
            analysis.setStatus(NutritionAnalysis.Status.FAILED);
            analysis.setErrorMessage("GMS " + e.getStatusCode() + ": " + responseBodyStr);
            nutritionMapper.updateNutritionAnalysis(analysis);
        } catch (Exception e) {
            log.error("[영양분석] videoId={} 최종 처리 실패: {}", videoId, e.getMessage(), e);
            analysis.setStatus(NutritionAnalysis.Status.FAILED);
            analysis.setErrorMessage(e.getMessage());
            nutritionMapper.updateNutritionAnalysis(analysis);
        } finally {
            // 🌟 3. 호스트 스토리지 풀릭 방지를 위한 캐시 버퍼 리셋 파일 삭제
            if (tempVideoFile != null && tempVideoFile.exists()) {
                tempVideoFile.delete();
            }
        }
    }

    private double nullSafe(Double val) {
        return val == null ? 0.0 : val;
    }

    private List<RecognizedFoodItem> parseAnalysisResult(String json, Long analysisId) {
        try {
            Map<String, Object> parsed = objectMapper.readValue(json, new TypeReference<>() {});
            List<Map<String, Object>> foods = (List<Map<String, Object>>) parsed.get("foods");

            List<RecognizedFoodItem> items = new ArrayList<>();
            if (foods == null) return items;

            for (Map<String, Object> food : foods) {
                RecognizedFoodItem item = new RecognizedFoodItem();
                item.setNutritionAnalysisId(analysisId);
                item.setFoodName((String) food.get("foodName"));
                item.setCalories(toDouble(food.get("calories")));
                item.setCarbs(toDouble(food.get("carbs")));
                item.setProtein(toDouble(food.get("protein")));
                item.setFat(toDouble(food.get("fat")));
                items.add(item);
            }
            return items;
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    private Double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}