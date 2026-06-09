package com.ssafy.yamyam.global.interceptor;

import com.ssafy.yamyam.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 메서드로 들어오는 Preflight 요청은 가볍게 통과시켜 줍니다 (CORS 방어)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 헤더에서 Authorization 추출
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. 토큰이 없거나 Bearer 형식이 아니면 410 Unauthorized 반환
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "인증 토큰이 누락되었거나 유효하지 않습니다.");
            return false; // 컨트롤러로 못 가게 막음
        }

        String token = authHeader.substring(7); // "Bearer " 제거

        try {
            // 3. 토큰 만료 여부 및 위변조 검증
            if (jwtUtil.isTokenExpired(token)) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "만료된 토큰입니다.");
                return false;
            }

            // 4. 토큰 클레임에서 유저 PK(id) 꺼내기
            Claims claims = jwtUtil.extractClaims(token);
            Long loginUserKey = ((Number) claims.get("id")).longValue();

            // 5. 💡 대박 중요: request 객체에 유저 고유 ID를 담아서 컨트롤러로 넘겨줌!
            request.setAttribute("loginUserKey", loginUserKey);
            return true; // 컨트롤러 진입 허용

        } catch (Exception e) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "인증 실패: " + e.getMessage());
            return false;
        }
    }
}