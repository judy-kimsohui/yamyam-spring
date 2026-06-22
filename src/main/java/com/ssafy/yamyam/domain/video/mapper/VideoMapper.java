package com.ssafy.yamyam.domain.video.mapper;

import com.ssafy.yamyam.domain.nutrition.model.RecognizedFoodItem;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.model.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoMapper {
    void insertVideo(Video video);
    List<VideoDto> findVideosByTeamAndDate(@Param("teamId") Long teamId, @Param("mealDate") String mealDate, @Param("userId") Long userId);
    List<VideoDto> findVideosByUserAndDate(@Param("ownerId") Long ownerId, @Param("mealDate") String mealDate, @Param("userId") Long userId);
    VideoDto findById(@Param("id") Long id, @Param("userId") Long userId);
    void deleteById(@Param("id") Long id);
    void updateAnalysis(Video video);

    // 내가 이 비디오를 좋아요 누를 때
    void insertLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    // 내가 이 비디오 좋아요를 해제할 때
    void deleteLike(@Param("videoId") Long videoId, @Param("userId") Long userId);

    // 내가 이 비디오에 좋아요를 눌렀었는가
    int existsLike(@Param("videoId") Long videoId, @Param("userId") Long userId);
    int countLikes(@Param("videoId") Long videoId);


    // ai comment 창에서 먹은 양 조절
    void updateFoodItemQuantity(@Param("id") Long id, @Param("quantity") Double quantity);

    //
    void updateNutritionAnalysisTotal(
            @Param("videoId") Long videoId,
            @Param("totalCalories") Double totalCalories,
            @Param("totalCarbs") Double totalCarbs,
            @Param("totalProtein") Double totalProtein,
            @Param("totalFat") Double totalFat
    );

    RecognizedFoodItem findFoodItemById(Long id);

    List<VideoDto> findTeamVideos(Long teamId, String date, Long loginUserId);
}
