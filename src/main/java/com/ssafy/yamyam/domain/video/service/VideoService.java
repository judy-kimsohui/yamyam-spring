package com.ssafy.yamyam.domain.video.service;

import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import com.ssafy.yamyam.domain.nutrition.service.VideoAnalysisService;
import com.ssafy.yamyam.domain.video.dto.FoodItemUpdateDto;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import com.ssafy.yamyam.domain.video.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;
    private final VideoStorage videoStorage;
    private final NutritionMapper nutritionMapper;
    private final VideoAnalysisService videoAnalysisService;


    @Transactional
    public VideoDto uploadVideo(Long userId, Long teamId, String mealType,
                                String mealDate, String description, MultipartFile videoFile) {
        String stored = videoStorage.save(videoFile);

        try {
            Video video = new Video();
            video.setUserId(userId);
            video.setTeamId(teamId);
            video.setMealType(mealType.toUpperCase());
            video.setMealDate(LocalDate.parse(mealDate));
            video.setVideoUrl(stored);
            video.setDescription(description);

            // 2. DB 저장 (MyBatis가 selectKey 등으로 video.setId()를 채워준다고 가정)
            videoMapper.insertVideo(video);


            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    videoAnalysisService.analyzeAsync(video.getId(), stored);
                }
            });


            VideoDto dto = buildDto(video);
            dto.setVideoUrl(videoStorage.toUrl(stored)); // 프론트엔드용 URL 변환
            return dto;

        } catch (Exception e) {
            // DB 작업 중 에러 발생 시, 스토리지에 저장했던 파일도 롤백(삭제) 처리하여 좀비 파일 방지
            videoStorage.delete(stored);
            throw e; // 예외를 다시 던져 트랜잭션 롤백 유도
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
