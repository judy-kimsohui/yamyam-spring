package com.ssafy.yamyam.domain.video.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.yamyam.domain.video.dto.FoodItemUpdateDto;
import com.ssafy.yamyam.domain.video.dto.PresignedUploadResult;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.dto.VideoRegisterDto;
import com.ssafy.yamyam.domain.video.service.VideoService;
import com.ssafy.yamyam.domain.video.service.VideoStorage;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final VideoStorage videoStorage;

    @GetMapping("/presigned-upload")
    public ResponseEntity<?> getPresignedUploadUrl(
            @RequestParam(defaultValue = "video/mp4") String contentType) {
        PresignedUploadResult result = videoStorage.presignPut(contentType);
        if (result == null) {
            return ResponseEntity.status(404).body("presigned upload not available in dev mode");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVideo(
            @RequestBody VideoRegisterDto dto,
            HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        try {
            VideoDto result = videoService.registerVideo(loginUserId, dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("등록 오류: " + e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("teamId") Long teamId,
            @RequestParam("mealType") String mealType,
            @RequestParam("mealDate") String mealDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("videoFile") MultipartFile videoFile,
            HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        try {
            VideoDto result = videoService.uploadVideo(loginUserId, teamId, mealType, mealDate, description, videoFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("업로드 오류: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVideo(@PathVariable Long id, HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        VideoDto dto = videoService.getVideoById(id, loginUserId);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id, HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        try {
            videoService.deleteVideo(id, loginUserId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/hls")
    public ResponseEntity<Void> triggerHls(@PathVariable Long id) {
        try {
            videoService.triggerHlsById(id);
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id, HttpServletRequest request) {
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        return ResponseEntity.ok(videoService.toggleLike(id, loginUserId));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<VideoDto>> getTeamVideos(
            @PathVariable Long teamId,
            @RequestParam(value = "date", required = false) String date,
            HttpServletRequest request) {
        if (date == null) date = LocalDate.now().toString();
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        return ResponseEntity.ok(videoService.getTeamVideos(teamId, date, loginUserId));
    }
    
    @PatchMapping("/{videoId}/quantities")
    public ResponseEntity<Void> updateQuantities(
            @PathVariable Long videoId,
            @RequestBody List<FoodItemUpdateDto> dtoList) {
        
        // 작성해두신 서비스 메서드 호출
        videoService.updateMealLogQuantities(videoId, dtoList);
        return ResponseEntity.ok().build();
    }
}
