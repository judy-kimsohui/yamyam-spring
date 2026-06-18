package com.ssafy.yamyam.domain.video.mapper;

import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.model.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoMapper {
    void insertVideo(Video video);
    List<VideoDto> findVideosByTeamAndDate(@Param("teamId") Long teamId, @Param("mealDate") String mealDate, @Param("userId") Long userId);
    VideoDto findById(@Param("id") Long id, @Param("userId") Long userId);
    void deleteById(@Param("id") Long id);
    void updateAnalysis(Video video);
    void insertLike(@Param("videoId") Long videoId, @Param("userId") Long userId);
    void deleteLike(@Param("videoId") Long videoId, @Param("userId") Long userId);
    int existsLike(@Param("videoId") Long videoId, @Param("userId") Long userId);
    int countLikes(@Param("videoId") Long videoId);
}
