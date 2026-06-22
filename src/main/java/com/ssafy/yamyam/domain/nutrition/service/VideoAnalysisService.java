package com.ssafy.yamyam.domain.nutrition.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoAnalysisService {

    // prod 프로파일에서만 S3Client 빈이 생성되므로 optional 주입
    @Autowired(required = false)
    private S3Client s3Client;

    @Value("${cloud.aws.s3.bucket:}")
    private String bucket;

    @Value("${yamyam.video.upload-dir:src/main/resources/static/videos}")
    private String uploadDir;

    @Value("${spring.ai.openai.base-url:https://gms.ssafy.io/gmsapi}")
    private String gmsBaseUrl;

    @Value("${spring.ai.openai.api-key}")
    private String gmsApiKey;

    private final NutritionMapper nutritionMapper;
    private final VideoFrameExtractor frameExtractor;
    private final ObjectMapper objectMapper;

    private static final String ANALYSIS_PROMPT = """
    	    제공된 이미지들은 사용자가 섭취하는 음식 및 음료의 연속 스냅샷입니다.
    	    이를 종합적으로 분석하여 아래 지정된 JSON 형식으로만 응답해 주세요. 다른 설명이나 텍스트는 절대 포함하지 마십시오.
    	    
    	    [분석 및 추정 가이드라인]
    	    1. 일반 요리: 영상 속 음식의 실제 대략적인 양과 부피를 고려하여 1의 자리 단위까지 정밀하게 추정하십시오.
    	    
    	    2. 공산품 및 포장 식품 (과자, 컵라면, 가공식품 등):
    	       - 봉지나 용기의 표면 디자인, 브랜드 로고, 제품명 텍스트를 식별하십시오.
    	       - 식별된 제품의 표준 영양성분표(용량 대비 칼로리/탄단지)를 바탕으로, 사진에 보이는 섭취량을 고려해 계산하십시오.
    	    
    	    3. 컵/용기에 담긴 음료 (테이크아웃 커피, 프랜차이즈 음료, 캔음료 등):
    	       - 일회용 플라스틱 컵, 플라스틱 홀더의 로고, 캔의 디자인을 통해 브랜드 및 메뉴를 추론하십시오.
    	       - 액체의 색상, 층 분리(라떼류), 크림/토핑 유무, 투명도를 분석하여 음료의 종류(예: 아메리카노, 라떼, 스무디 등)를 판별하고 규격 사이즈 기준의 영양소를 산출하십시오.
    	    
    	    4. 수치를 10 단위로 뭉뚱그려 응답하지 말고, 최대한 실제 수치에 가깝게 정밀하게 추정하십시오 (예: 523, 42.5 등).
    	    5. 음식이 전혀 보이지 않거나 식별이 불가능하면 foods를 빈 배열([])로 반환하십시오.
    	    
    	    {
    	      "foods": [
    	        {
    	          "foodName": "음식 또는 제품 이름 (한국어, 브랜드명 포함 가능)",
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
        boolean isS3Download = false;

        try {
            boolean isLocalPath = storedVideoPath.startsWith("/videos/");

            if (isLocalPath) {
                // -- dev: 로컬 디스크에서 직접 읽기 ------------------------------
                File folder = new File(uploadDir).isAbsolute()
                        ? new File(uploadDir)
                        : new File(System.getProperty("user.dir"), uploadDir);
                String filename = storedVideoPath.substring("/videos/".length());
                File localFile = new File(folder, filename);
                storedVideoPath = localFile.getAbsolutePath();
                log.info("[영양분석] videoId={} 로컬 파일 분석 시작: {}", videoId, storedVideoPath);
            } else {
                // -- prod: S3에서 임시 다운로드 -----------------------------------
                if (s3Client == null) {
                    throw new IllegalStateException("S3Client 빈이 없습니다. prod 프로파일로 실행하세요.");
                }

                String bucketName = this.bucket;
                String s3Key = storedVideoPath;

                if (storedVideoPath.startsWith("s3://")) {
                    java.net.URI uri = new java.net.URI(storedVideoPath);
                    bucketName = uri.getHost();
                    String path = uri.getPath();
                    s3Key = path.startsWith("/") ? path.substring(1) : path;
                } else if (storedVideoPath.startsWith("http://") || storedVideoPath.startsWith("https://")) {
                    java.net.URI uri = new java.net.URI(storedVideoPath);
                    String host = uri.getHost();
                    if (host != null && host.contains(".")) bucketName = host.split("\\.")[0];
                    if (storedVideoPath.contains("videos/")) {
                        s3Key = storedVideoPath.substring(storedVideoPath.indexOf("videos/"));
                        if (s3Key.contains("?")) s3Key = s3Key.substring(0, s3Key.indexOf("?"));
                    } else {
                        String path = uri.getPath();
                        s3Key = path.startsWith("/") ? path.substring(1) : path;
                    }
                }

                log.info("[영양분석] videoId={} S3 다운로드 시작 (Bucket: {}, Key: {})", videoId, bucketName, s3Key);
                File appDir = new File(System.getProperty("user.dir"));
                String tempFileName = "s3_video_" + videoId + "_" + System.currentTimeMillis() + ".mp4";
                tempVideoFile = new File(appDir, tempFileName);
                isS3Download = true;

                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build();
                s3Client.getObject(getObjectRequest, software.amazon.awssdk.core.sync.ResponseTransformer.toFile(tempVideoFile));
                storedVideoPath = tempVideoFile.getAbsolutePath();
                log.info("[영양분석] videoId={} S3 다운로드 완료 -> OpenCV 프레임 추출 시작", videoId);
            }
            List<byte[]> frames = frameExtractor.extractFrames(storedVideoPath);

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

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", "gpt-4o");
            requestBody.put("messages", List.of(messagePayload));
            requestBody.put("temperature", 0.2);

            log.info("[영양분석] videoId={} GMS AI 영양소 식별 매핑 전송", videoId);

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

            log.info("[영양분석] videoId={} GPT-4o 분석 응답 파싱 수신 완료", videoId);

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

            log.info("[영양분석] videoId={} 영양 지표 최종 마감 — {}kcal, 음식 {}종",
                    videoId, totalCalories, foodItems.size());

        } catch (WebClientResponseException e) {
            String responseBodyStr = e.getResponseBodyAsString();
            log.error("[영양분석] videoId={} GMS API 오류 응답 status={} body={}",
                    videoId, e.getStatusCode(), responseBodyStr, e);
            analysis.setStatus(NutritionAnalysis.Status.FAILED);
            analysis.setErrorMessage("GMS " + e.getStatusCode() + ": " + responseBodyStr);
            nutritionMapper.updateNutritionAnalysis(analysis);
        } catch (Exception e) {
            log.error("[영양분석] videoId={} 인프라 로드 최종 실패: {}", videoId, e.getMessage(), e);
            analysis.setStatus(NutritionAnalysis.Status.FAILED);
            analysis.setErrorMessage(e.getMessage());
            nutritionMapper.updateNutritionAnalysis(analysis);
        } finally {
            // S3에서 내려받은 임시 파일만 삭제 (로컬 원본 파일은 건드리지 않음)
            if (isS3Download && tempVideoFile != null && tempVideoFile.exists()) {
                if (tempVideoFile.delete()) {
                    log.info("[영양분석] videoId={} S3 임시 파일 삭제 완료", videoId);
                }
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