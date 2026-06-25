package com.ssafy.yamyam.domain.video.service;

import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;
import com.ssafy.yamyam.domain.nutrition.model.NutritionAnalysis;
import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import com.ssafy.yamyam.domain.nutrition.service.VideoAnalysisService;
import com.ssafy.yamyam.domain.video.dto.FoodItemUpdateDto;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.dto.VideoRegisterDto;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import com.ssafy.yamyam.domain.video.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
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

    @Nullable
    @Autowired(required = false)
    private HlsTranscoderService hlsTranscoder;


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
                    if (hlsTranscoder != null) hlsTranscoder.transcodeAsync(video.getId(), stored); //
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

    @Transactional
    public VideoDto registerVideo(Long userId, VideoRegisterDto dto) {
        String stored = dto.getKey();

        Video video = new Video();
        video.setUserId(userId);
        video.setTeamId(dto.getTeamId());
        video.setMealType(dto.getMealType().toUpperCase());
        video.setMealDate(LocalDate.parse(dto.getMealDate()));
        video.setVideoUrl(stored);
        video.setDescription(dto.getDescription());

        videoMapper.insertVideo(video);

        NutritionAnalysis initialAnalysis = new NutritionAnalysis();
        initialAnalysis.setVideoId(video.getId());
        initialAnalysis.setStatus(NutritionAnalysis.Status.PENDING);
        nutritionMapper.insertNutritionAnalysis(initialAnalysis);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                videoAnalysisService.analyzeAsync(video.getId(), stored);
                if (hlsTranscoder != null) hlsTranscoder.transcodeAsync(video.getId(), stored);
            }
        });

        VideoDto result = buildDto(video);
        result.setVideoUrl(videoStorage.toUrl(stored));
        result.setStatus("PENDING");
        return result;
    }

    public void triggerHlsById(Long videoId) {
        if (hlsTranscoder == null) return;
        VideoDto dto = videoMapper.findById(videoId, 0L);
        if (dto == null) throw new IllegalArgumentException("Video not found: " + videoId);
        hlsTranscoder.transcodeAsync(videoId, dto.getVideoUrl());
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
