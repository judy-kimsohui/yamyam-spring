package com.ssafy.yamyam.domain.user.controller;

import com.ssafy.yamyam.domain.user.dto.LoginRequestDto;
import com.ssafy.yamyam.domain.user.dto.UserDto;
import com.ssafy.yamyam.domain.user.dto.UserUpdateRequestDto;
import com.ssafy.yamyam.domain.user.model.User;
import com.ssafy.yamyam.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.yamyam.domain.user.dto.SignupRequestDto;
import com.ssafy.yamyam.domain.user.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
//@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "https://reqbin.com"})
//@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 폼 데이터 통합 회원가입 테스트 API
     * POST http://localhost:8080/api/users/signup
     * * 주의: @RequestBody가 아닌 @ModelAttribute를 사용해야
     * 텍스트 필드들과 프로필 이미지(MultipartFile)를 동시에 바인딩할 수 있습니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@ModelAttribute SignupRequestDto signupDto) {
        try {
            userService.signup(signupDto);
            return ResponseEntity.ok("회원가입 성공! 파일 업로드 및 전체 스펙 정보가 안전하게 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }


    // 2. 로그인 (POST /api/users/login)
    // 💡 JSON 데이터를 받아야 하므로 @RequestBody를 꼭 붙여줍니다.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginDto) {
        try {
            // 서비스 로직을 통해 ID/PW 검증
            User loginUser = userService.login(loginDto.getUserId(), loginDto.getPassword());

            // 인증 성공 시 세션 생성 (기존 세션이 있으면 반환, 없으면 신규 생성)
            String token = jwtUtil.generateToken(loginUser.getId(), loginUser.getUserId());

            // 토큰 정보와 안내 메세지 Front에 전달.
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("message", "로그인 성공!");
            responseData.put("nickName", loginUser.getNickName());

            return ResponseEntity.ok(responseData);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 로그아웃 (POST /api/users/logout) (브라우저 스토리지의 토큰을 삭제)
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("로그아웃 되었습니다. ");
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 누락되었거나 유효하지 않습니다.");
        }

        String token = authHeader.substring(7);

        try {

            if (jwtUtil.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("만료된 토큰입니다.");
            }


            Claims claims = jwtUtil.extractClaims(token);
            Long loginUserKey = ((Number) claims.get("id")).longValue();

            UserDto userDto = userService.findUserById(loginUserKey);
            return ResponseEntity.ok(userDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 실패: " + e.getMessage());
        }	
    }

    // UserController.java 파일 내부에 추가

    @PutMapping("/profile") // 💡 프론트엔드의 axios.put("/api/users/profile") 요청을 여기서 받습니다.
    public ResponseEntity<String> updateProfile(
            @RequestBody com.ssafy.yamyam.domain.user.dto.UserUpdateRequestDto updateDto,
            jakarta.servlet.http.HttpServletRequest request) { // 💡 세션 대신 request 활용

        Long loginUserKey = (Long) request.getAttribute("loginUserKey");

        if (loginUserKey == null) {
            return org.springframework.http.ResponseEntity.status(401).body("인증 정보가 만료되었습니다.");
        }

        try {
            userService.updateProfile(loginUserKey, updateDto);
            return ResponseEntity.ok("프로필 정보가 안전하게 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류: " + e.getMessage());
        }
    }
}