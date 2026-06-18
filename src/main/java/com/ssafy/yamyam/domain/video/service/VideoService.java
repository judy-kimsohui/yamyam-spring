package com.ssafy.yamyam.domain.video.service;

import com.ssafy.yamyam.domain.nutrition.service.VideoAnalysisService;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import com.ssafy.yamyam.domain.video.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;
    private final VideoStorage videoStorage;
    private final VideoAnalysisService videoAnalysisService;

    @Transactional
    public VideoDto uploadVideo(Long userId, Long teamId, String mealType,
                                String mealDate, String description, MultipartFile videoFile) {
        String stored = videoStorage.save(videoFile);

        Video video = new Video();
        video.setUserId(userId);
        video.setTeamId(teamId);
        video.setMealType(mealType.toUpperCase());
        video.setMealDate(LocalDate.parse(mealDate));
        video.setVideoUrl(stored);
        video.setDescription(description);

        videoMapper.insertVideo(video);

        // AI가 영상을 분석하는
        videoAnalysisService.analyzeAsync(video.getId(), stored);


        VideoDto dto = new VideoDto();
        dto.setId(video.getId());
        dto.setUserId(userId);
        dto.setTeamId(teamId);
        dto.setMealType(video.getMealType());
        dto.setMealDate(mealDate);
        dto.setVideoUrl(videoStorage.toUrl(stored));
        return dto;
    }

    public List<VideoDto> getTeamVideos(Long teamId, String mealDate) {
        List<VideoDto> videos = videoMapper.findVideosByTeamAndDate(teamId, mealDate);
        videos.forEach(v -> v.setVideoUrl(videoStorage.toUrl(v.getVideoUrl())));
        return videos;
    }
}
