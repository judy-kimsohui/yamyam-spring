package com.ssafy.yamyam.domain.video.controller;

import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
//@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("teamId") Long teamId,
            @RequestParam("mealType") String mealType,
            @RequestParam("mealDate") String mealDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("videoFile") MultipartFile videoFile,
            HttpServletRequest request) { // 💡 HttpSession -> HttpServletRequest 변경

        Long loginUserKey = (Long) request.getAttribute("loginUserKey");

        try {
            VideoDto result = videoService.uploadVideo(loginUserKey, teamId, mealType, mealDate, description, videoFile);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("업로드 오류: " + e.getMessage());
        }
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<VideoDto>> getTeamVideos(
            @PathVariable Long teamId,
            @RequestParam(value = "date", required = false) String date) {
        if (date == null) date = LocalDate.now().toString();
        return ResponseEntity.ok(videoService.getTeamVideos(teamId, date));
    }


}
