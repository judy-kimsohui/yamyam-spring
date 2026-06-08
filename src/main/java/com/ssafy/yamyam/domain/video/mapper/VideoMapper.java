package com.ssafy.yamyam.domain.video.mapper;

import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.model.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoMapper {
    void insertVideo(Video video);
    List<VideoDto> findVideosByTeamAndDate(@Param("teamId") Long teamId, @Param("mealDate") String mealDate);
}
