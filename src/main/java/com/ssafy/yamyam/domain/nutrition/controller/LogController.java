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
        try {
            Long userId = extractUserId(authHeader);
            String aiComment = dailyAiService.evaluateDailyLog(userId, date);

            return ResponseEntity.ok(Map.of("aiComment", aiComment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("평가 실패: " + e.getMessage());
        }
    }

    @GetMapping("/daily/ai-comment")
    public ResponseEntity<?> getDailyAiComment(@RequestParam String date,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        try {
            Long userId = extractUserId(authHeader);
            String aiComment = dailyAiService.getDailyAiComment(userId, date);
            return ResponseEntity.ok(Map.of("aiComment", aiComment == null ? "" : aiComment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("피드백 조회 실패: " + e.getMessage());
        }
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 필요합니다.");
        }
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.extractClaims(token);
        return ((Number) claims.get("id")).longValue();
    }
}
