package com.ssafy.yamyam.domain.video.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import com.ssafy.yamyam.domain.nutrition.service.VideoAnalysisService;
import com.ssafy.yamyam.domain.user.mapper.UserMapper;
import com.ssafy.yamyam.domain.video.dto.DailyDashboardResponseDto;
import com.ssafy.yamyam.domain.video.dto.DailyDietSummaryDto;
import com.ssafy.yamyam.domain.video.dto.FoodItemUpdateDto;
import com.ssafy.yamyam.domain.video.dto.NutrientGoalDto;
import com.ssafy.yamyam.domain.video.dto.PeriodNutrientTrendDto;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.mapper.LogMapper;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import com.ssafy.yamyam.domain.video.model.Video;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;
    private final LogMapper logMapper;
    private final NutritionMapper nutritionMapper;
    private final UserMapper userMapper;
    private final VideoStorage videoStorage;
    private final VideoAnalysisService videoAnalysisService;


    @Transactional
    public VideoDto uploadVideo(Long userId, Long teamId, String mealType,
                                String mealDate, String description, MultipartFile videoFile) {
        String stored = videoStorage.save(videoFile); //

        try {
            Video video = new Video(); //
            video.setUserId(userId); //
            video.setTeamId(teamId); //
            video.setMealType(mealType.toUpperCase()); //
            video.setMealDate(LocalDate.parse(mealDate)); //
            video.setVideoUrl(stored); //
            video.setDescription(description); //

            // 1. VIDEOS 테이블 저장 (video.setId() 발급)
            videoMapper.insertVideo(video); //

            // 🌟 [추가: 영양소 테이블 PENDING 상태 조기 선점] 
            // 프론트엔드가 업로드 직후 바로 조회 쿼리를 날려도 null이 안 뜨도록 원천 차단합니다.
            NutritionAnalysis initialAnalysis = new NutritionAnalysis();
            initialAnalysis.setVideoId(video.getId());
            initialAnalysis.setStatus(NutritionAnalysis.Status.PENDING);
            nutritionMapper.insertNutritionAnalysis(initialAnalysis);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { //
                @Override
                public void afterCommit() { //
                    videoAnalysisService.analyzeAsync(video.getId(), stored); //
                } //
            }); //

            VideoDto dto = buildDto(video); //
            dto.setVideoUrl(videoStorage.toUrl(stored));  //
            dto.setStatus("PENDING"); // 🌟 즉시 리턴되는 응답 객체에도 PENDING 주입
            return dto; //

        } catch (Exception e) { //
            videoStorage.delete(stored); //
            throw e;  //
        }
    }

    public VideoDto getVideoById(Long id, Long loginUserId) {
        long uid = loginUserId != null ? loginUserId : 0L;
        VideoDto dto = videoMapper.findById(id, uid);
        if (dto != null) dto.setVideoUrl(videoStorage.toUrl(dto.getVideoUrl()));
        return dto;
    }

    @Transactional
    public void deleteVideo(Long videoId, Long loginUserId) {
        VideoDto dto = videoMapper.findById(videoId, 0L);
        if (dto == null) throw new IllegalArgumentException("영상이 존재하지 않습니다.");
        if (!dto.getUserId().equals(loginUserId)) throw new SecurityException("본인 영상만 삭제할 수 있습니다.");
        videoStorage.delete(dto.getVideoUrl());
        videoMapper.deleteById(videoId);
    }

    public List<VideoDto> getMyVideos(Long ownerId, String mealDate, Long loginUserId) {
        long uid = loginUserId != null ? loginUserId : 0L;
        List<VideoDto> videos = videoMapper.findVideosByUserAndDate(ownerId, mealDate, uid);
        videos.forEach(v -> v.setVideoUrl(videoStorage.toUrl(v.getVideoUrl())));
        return videos;
    }

    public List<VideoDto> getTeamVideos(Long teamId, String mealDate, Long loginUserId) {
        long uid = loginUserId != null ? loginUserId : 0L;
        List<VideoDto> videos = videoMapper.findVideosByTeamAndDate(teamId, mealDate, uid);
        videos.forEach(v -> v.setVideoUrl(videoStorage.toUrl(v.getVideoUrl())));
        return videos;
    }




    @Transactional
    public Map<String, Object> toggleLike(Long videoId, Long userId) {
        boolean alreadyLiked = videoMapper.existsLike(videoId, userId) > 0;
        if (alreadyLiked) {
            videoMapper.deleteLike(videoId, userId);
        } else {
            videoMapper.insertLike(videoId, userId);
        }
        int count = videoMapper.countLikes(videoId);
        return Map.of("liked", !alreadyLiked, "count", count);
    }


    @Transactional
    public void updateMealLogQuantities(Long videoId, List<FoodItemUpdateDto> paramList){

        double newTotalCalories = 0;
        double newTotalCarbs = 0;
        double newTotalProtein = 0;
        double newTotalFat = 0;

        for (FoodItemUpdateDto param : paramList) {

            // [Step A] 이미 DB에 들어있는 1인분 기준 영양소 값을 가져옴
            // (이를 위해 Mapper에 특정 음식 항목 하나를 조회하는 findFoodItemById 매퍼가 필요합니다)
            RecognizedFoodItem foodItem = videoMapper.findFoodItemById(param.getId());

            if (foodItem == null) {
                throw new IllegalArgumentException("존재하지 않는 음식 항목입니다. ID: " + param.getId());
            }

            // [Step B] 프론트가 보낸 '조절한 인분'을 해당 음식 레코드에 먼저 업데이트
            videoMapper.updateFoodItemQuantity(param.getId(), param.getQuantity());

            // [Step C] DB에 있던 원본 영양소(1인분 기준)에 사용자가 조절한 인분(quantity)을 곱해서 누적
            // getCalories(), getCarbs() 등은 recognized_food_item 테이블 컬럼과 매핑된 실제 메서드입니다.
            newTotalCalories += foodItem.getCalories() * param.getQuantity();
            newTotalCarbs    += foodItem.getCarbs()    * param.getQuantity();
            newTotalProtein  += foodItem.getProtein()  * param.getQuantity();
            newTotalFat      += foodItem.getFat()      * param.getQuantity();
        }

        // 3. 최종적으로 계산이 끝난 총합 데이터를 nutrition_analysis 테이블에 업데이트
        // DTO를 새로 만들지 않고 @Param으로 찢어서 넘기도록 매퍼 시그니처에 맞춤
        videoMapper.updateNutritionAnalysisTotal(
                videoId,
                newTotalCalories,
                newTotalCarbs,
                newTotalProtein,
                newTotalFat
        );
    }
    
 // VideoService.java 내부에 추가할 메서드

    /**
     * [만능 수집기] 특정 유저의 특정 날짜 식단/체중 원천 데이터를 한 바구니로 조립합니다.
     * - 오늘 날짜를 넣으면: 오늘 실시간 누적치 계산 (초과/부족량 측정용)
     * - 어제 날짜를 넣으면: 어제 하루치 데이터 수집 (정기 AI 피드백 생성용)
     */
    public DailyDietSummaryDto getDailyDietSummary(Long userId, String targetDate) {

    	DailyDietSummaryDto summary = logMapper.findDailyNutrientSum(userId, targetDate);
        
        if (summary == null) {
            summary = new DailyDietSummaryDto();
            summary.setTotalCalories(0.0);
            summary.setTotalCarbs(0.0);
            summary.setTotalProtein(0.0);
            summary.setTotalFat(0.0);
        }

        
        List<String> foodNames = logMapper.findDailyFoodNames(userId, targetDate);
        summary.setFoodNames(foodNames);

      
        Double recordedWeight = logMapper.findDailyWeight(userId, targetDate);
        summary.setRecordedWeight(recordedWeight);

        return summary;
    }

    
    public NutrientGoalDto calculateUserNutrientGoal(Long userId) {
        // 1. 유저 마스터 정보 획득
        com.ssafy.yamyam.domain.user.model.User user = userMapper.findById(userId);
        
        NutrientGoalDto goal = new NutrientGoalDto();
        
        // 방어 가드: 유저 정보가 없거나 몸무게/키 입력이 누락된 경우 기본 표준 스펙(2000kcal) 지정
        if (user == null || user.getWeight() <= 0 || user.getHeight() <= 0) {
            goal.setTargetCalories(2000.0);
            goal.setTargetCarbs(250.0);
            goal.setTargetProtein(150.0);
            goal.setTargetFat(44.0);
            return goal;
        }

        double weight = user.getWeight();
        double height = user.getHeight();
        int age = user.getAge();
        
        // 2. 해리스-베네딕트 공식을 통한 활동대사량(BMR * 활동계수 약 1.375) 산출
        double bmr = 0.0;
        if (user.getGender() == com.ssafy.yamyam.domain.user.model.User.Gender.MALE) {
            bmr = 66.47 + (13.75 * weight) + (5.0 * height) - (6.76 * age);
        } else if (user.getGender() == com.ssafy.yamyam.domain.user.model.User.Gender.FEMALE) {
            bmr = 655.1 + (9.56 * weight) + (1.85 * height) - (4.68 * age);
        } else {
            bmr = 22 * weight * 30; // 성별 미지정 시 일반 표준 기초 대사 공식 적용
        }
        
        double targetCalories = Math.round(bmr * 1.375); // 일반적인 활동량 계수 반영
        goal.setTargetCalories(targetCalories);

        // 3. 탄단지 황금 비율(5:3:2)에 맞게 그람(g) 수 역산
        // 탄수화물(4kcal/g), 단백질(4kcal/g), 지방(9kcal/g)
        goal.setTargetCarbs(Math.round((targetCalories * 0.5) / 4.0));
        goal.setTargetProtein(Math.round((targetCalories * 0.3) / 4.0));
        goal.setTargetFat(Math.round((targetCalories * 0.2) / 9.0));

        return goal;
    }
    
    
    
    /**
     * [하루 대시보드 마스터 - 최종 진화형]
     * 실시간 섭취량 + 계산된 목표치 + 새벽에 구워둔 정기 AI 피드백까지 한 그릇으로 조립합니다.
     */
    public DailyDashboardResponseDto getDailyDashboardReport(Long userId, String targetDate) {
        DailyDashboardResponseDto response = new DailyDashboardResponseDto();
        
        // ① 유저 권장 목표치 계산 및 바인딩
        NutrientGoalDto goal = calculateUserNutrientGoal(userId);
        response.setGoal(goal);
        
        // ② 해당 날짜 실시간 완료 데이터 계산 및 바인딩
        DailyDietSummaryDto summary = getDailyDietSummary(userId, targetDate);
        response.setSummary(summary);
        
        // ③ [산술 연산] 목표치 - 현재 누적 섭취량 (남은 잔여량)
        response.setRemainCalories(goal.getTargetCalories() - summary.getTotalCalories());
        response.setRemainCarbs(goal.getTargetCarbs() - summary.getTotalCarbs());
        response.setRemainProtein(goal.getTargetProtein() - summary.getTotalProtein());
        response.setRemainFat(goal.getTargetFat() - summary.getTotalFat());
        
        // ④ [실시간 규칙 기반 즉석 피드백 조립]
        StringBuilder feedback = new StringBuilder();
        if (summary.getTotalCalories() == 0) {
            feedback.append("아직 오늘 기록된 식단이 없습니다. 먹은 첫 끼니의 영상을 공유해 보세요! 🍽️");
        } else {
            double calorieRatio = (summary.getTotalCalories() / goal.getTargetCalories()) * 100;
            feedback.append(String.format("오늘 목표 칼로리의 %.1f%%를 채우셨군요! ", calorieRatio));
            
            if (summary.getTotalProtein() < goal.getTargetProtein() * 0.5) {
                feedback.append("목표 대비 단백질 섭취가 많이 부족합니다. 다음 식사엔 단백질 위주로 챙겨보세요. 🥚");
            } else if (summary.getTotalCarbs() > goal.getTargetCarbs() * 1.1) {
                feedback.append("탄수화물 비율이 다소 높은 편입니다. 정제 탄수화물은 조금 제어해 보시는 걸 권장해요. 🛑");
            } else {
                feedback.append("탄단지 영양 밸런스를 아주 훌륭하게 유지하며 식단 관리를 잘 수행하고 계십니다! ✨");
            }
        }
        response.setRuleFeedback(feedback.toString());
        
        // ⑤ 🌟 [신설]: 데이터베이스에 미리 저장된 1일 1회 정기 AI 피드백 총평 결합
        // 사용자가 '어제 날짜' 탭을 누르거나 대시보드 진입 시 미리 구워진 리포트 텍스트가 있다면 가져옴
        String savedAiFeedback = logMapper.findAiFeedback(userId, targetDate);
        response.setAiDailyFeedback(savedAiFeedback != null ? savedAiFeedback : "해당 날짜의 AI 정기 리포트가 아직 생성되지 않았거나 없습니다.");
        
        return response;
    }
    
    public List<PeriodNutrientTrendDto> getNutrientTrend(Long userId, String startDate, String endDate) {
        List<PeriodNutrientTrendDto> trends = logMapper.findNutrientTrendByPeriod(userId, startDate, endDate);
        
        // 부동 소수점 오차를 백엔드 레이어에서 깔끔하게 단정화 (반올림 처리)
        for (PeriodNutrientTrendDto trend : trends) {
            trend.setDailyCalories(Math.round(trend.getDailyCalories()));
            trend.setDailyCarbs(Math.round(trend.getDailyCarbs() * 10.0) / 10.0);
            trend.setDailyProtein(Math.round(trend.getDailyProtein() * 10.0) / 10.0);
            trend.setDailyFat(Math.round(trend.getDailyFat() * 10.0) / 10.0);
        }
        
        return trends;
    }

    private VideoDto buildDto(Video v) {
        VideoDto dto = new VideoDto();
        dto.setId(v.getId());
        dto.setUserId(v.getUserId());
        dto.setTeamId(v.getTeamId());
        dto.setMealType(v.getMealType());
        dto.setMealDate(v.getMealDate() != null ? v.getMealDate().toString() : null);
        dto.setVideoUrl(v.getVideoUrl());
        dto.setDescription(v.getDescription());
        dto.setCalories(v.getCalories());
        dto.setCarbs(v.getCarbs());
        dto.setProtein(v.getProtein());
        dto.setFat(v.getFat());
        dto.setAiComment(v.getAiComment());
        return dto;
    }
}
