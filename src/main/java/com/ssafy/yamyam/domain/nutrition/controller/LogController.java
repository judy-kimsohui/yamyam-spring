package com.ssafy.yamyam.domain.nutrition.controller;

import com.ssafy.yamyam.domain.nutrition.service.LogService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    
    private final LogService logService;

    // GET /api/logs/daily?date=2026-06-24
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyLogs(@RequestParam String date, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        // 날짜별 요약 + 상세 데이터를 Map으로 반환받아 전달
        Map<String, Object> data = logService.getDailyLogData(userId, date);
        return ResponseEntity.ok(data);
    }
}