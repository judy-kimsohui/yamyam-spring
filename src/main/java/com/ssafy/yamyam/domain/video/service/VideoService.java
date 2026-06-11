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

        // AI 분석 (실패해도 업로드는 성공으로 처리)
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

    public VideoDto getVideoById(Long id) {
        VideoDto dto = videoMapper.findById(id);
        if (dto != null) dto.setVideoUrl(videoStorage.toUrl(dto.getVideoUrl()));
        return dto;
    }

    @Transactional
    public void deleteVideo(Long videoId, Long loginUserId) {
        VideoDto dto = videoMapper.findById(videoId);
        if (dto == null) throw new IllegalArgumentException("영상이 존재하지 않습니다.");
        if (!dto.getUserId().equals(loginUserId)) throw new SecurityException("본인 영상만 삭제할 수 있습니다.");
        videoStorage.delete(dto.getVideoUrl()); // S3/로컬 파일 삭제
        videoMapper.deleteById(videoId);
    }

    public List<VideoDto> getTeamVideos(Long teamId, String mealDate) {
        List<VideoDto> videos = videoMapper.findVideosByTeamAndDate(teamId, mealDate);
        videos.forEach(v -> v.setVideoUrl(videoStorage.toUrl(v.getVideoUrl())));
        return videos;
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
