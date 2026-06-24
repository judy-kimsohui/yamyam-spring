package com.ssafy.yamyam.domain.video.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.yamyam.domain.video.dto.DailyDashboardResponseDto;
import com.ssafy.yamyam.domain.video.dto.PeriodNutrientTrendDto;
import com.ssafy.yamyam.domain.video.dto.VideoDto;
import com.ssafy.yamyam.domain.video.service.VideoService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/videos")
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
    
    /**
     * [하루 대시보드 API] 특정 날짜의 실시간 섭취량, 목표치, 잔여량, 실시간 규칙 피드백을 일괄 조회합니다.
     * GET /api/videos/dashboard/daily?date=2026-06-23
     */
    @GetMapping("/dashboard/daily")
    public ResponseEntity<DailyDashboardResponseDto> getDailyDashboard(
            @RequestParam("date") String date,
            HttpServletRequest request) {
        
        // 1. 요청 인터셉터나 필터가 세팅해 둔 유저 마스터 시스템 PK 꺼내기
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
        
        if (loginUserId == null) {
        	
            return ResponseEntity.status(401).build();
        }

        // 2. 서비스 레이어 마스터 메서드를 호출하여 완성된 하루 레포트 그릇 획득
        DailyDashboardResponseDto dashboardReport = videoService.getDailyDashboardReport(loginUserId, date);
        
        return ResponseEntity.ok(dashboardReport);
    }
    
    /**
     * [통계 꺾은선 그래프 API] 유저가 지정한 기간 동안의 일별 탄단지/칼로리 통계 데이터를 사출합니다.
     * GET /api/videos/dashboard/trend?startDate=2026-06-16&endDate=2026-06-23
     */
    @GetMapping("/dashboard/trend")
    public ResponseEntity<List<PeriodNutrientTrendDto>> getNutrientTrend(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            HttpServletRequest request) {
        
        Long loginUserId = (Long) request.getAttribute("loginUserKey");
    	if (loginUserId == null) {
        	
            return ResponseEntity.status(401).build();
        }

        List<PeriodNutrientTrendDto> trendData = videoService.getNutrientTrend(loginUserId, startDate, endDate);
        return ResponseEntity.ok(trendData);
    }
}
