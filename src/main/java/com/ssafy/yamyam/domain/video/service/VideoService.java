package com.ssafy.yamyam.domain.video.service;

import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.mapper.VideoMapper;
import com.ssafy.yamyam.domain.video.model.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoMapper videoMapper;

    @Value("${yamyam.video.upload-dir}")
    private String uploadDir;

    @Transactional
    public VideoDto uploadVideo(Long userId, Long teamId, String mealType,
                                String mealDate, String description, MultipartFile videoFile) {
        String savedFilename = saveVideoFile(videoFile);

        Video video = new Video();
        video.setUserId(userId);
        video.setTeamId(teamId);
        video.setMealType(mealType.toUpperCase());
        video.setMealDate(LocalDate.parse(mealDate));
        video.setVideoUrl("/videos/" + savedFilename);
        video.setDescription(description);

        videoMapper.insertVideo(video);

        VideoDto dto = new VideoDto();
        dto.setId(video.getId());
        dto.setUserId(userId);
        dto.setTeamId(teamId);
        dto.setMealType(video.getMealType());
        dto.setMealDate(mealDate);
        dto.setVideoUrl(video.getVideoUrl());
        return dto;
    }

    private String saveVideoFile(MultipartFile file) {
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf("."))
                : ".mp4";
        String filename = UUID.randomUUID().toString() + ext;

        try {
            file.transferTo(new File(folder, filename));
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("동영상 파일 저장 오류: " + e.getMessage(), e);
        }
    }

    public List<VideoDto> getTeamVideos(Long teamId, String mealDate) {
        return videoMapper.findVideosByTeamAndDate(teamId, mealDate);
    }
}
