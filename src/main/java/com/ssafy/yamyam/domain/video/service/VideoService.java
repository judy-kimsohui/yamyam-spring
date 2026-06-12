package com.ssafy.yamyam.domain.video.service;

import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import com.ssafy.yamyam.domain.video.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;
    private final VideoStorage videoStorage;
    private final VideoAnalysisService analysisService;

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

        analysisService.analyze(description, mealType).ifPresent(r -> {
            video.setCalories(r.getCalories());
            video.setCarbs(r.getCarbs());
            video.setProtein(r.getProtein());
            video.setFat(r.getFat());
            video.setAiComment(r.getComment());
            videoMapper.updateAnalysis(video);
        });

        VideoDto dto = buildDto(video);
        dto.setVideoUrl(videoStorage.toUrl(stored));
        dto.setUploaderNickName(null);
        return dto;
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
