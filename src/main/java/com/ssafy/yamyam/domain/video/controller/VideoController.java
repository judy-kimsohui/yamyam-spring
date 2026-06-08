package com.ssafy.yamyam.domain.video.controller;

import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.service.VideoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Value("${spring.servlet.multipart.location}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("teamId") Long teamId,
            @RequestParam("mealType") String mealType,
            @RequestParam("mealDate") String mealDate,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("videoFile") MultipartFile videoFile,
            HttpSession session) {

        Long loginUserKey = (Long) session.getAttribute("loginUserKey");
        if (loginUserKey == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

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

    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> serveVideo(@PathVariable String filename) {
        File file = new File(uploadDir + "/videos/" + filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = filename.endsWith(".webm") ? "video/webm" : "video/mp4";
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
