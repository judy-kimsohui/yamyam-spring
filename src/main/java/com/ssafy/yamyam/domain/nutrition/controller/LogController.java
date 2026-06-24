package com.ssafy.yamyam.domain.nutrition.controller;

import com.ssafy.yamyam.domain.nutrition.service.DailyAiService;
import com.ssafy.yamyam.domain.nutrition.service.LogService;

import com.ssafy.yamyam.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {
    
    private final LogService logService;
    private final DailyAiService dailyAiService;
    private final JwtUtil jwtUtil;

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

    @PostMapping("/daily/evaluate")
    public ResponseEntity<?> evaluateDailyLogs(@RequestParam String date,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("인증 토큰이 필요합니다.");
        }
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.extractClaims(token);
            Long userId = ((Number) claims.get("id")).longValue();

            // AI 평가 로직 수행
            String aiComment = dailyAiService.evaluateDailyLog(userId, date);

            return ResponseEntity.ok(Map.of("aiComment", aiComment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("평가 실패: " + e.getMessage());
        }
    }
}