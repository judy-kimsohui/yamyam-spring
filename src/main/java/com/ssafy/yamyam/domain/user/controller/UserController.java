package com.ssafy.yamyam.domain.user.controller;

import com.ssafy.yamyam.domain.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.yamyam.domain.user.dto.SignupRequestDto;
import com.ssafy.yamyam.domain.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
    public ResponseEntity<String> login(
            @RequestBody com.ssafy.yamyam.domain.user.dto.LoginRequestDto loginDto,
            HttpServletRequest request) {
        try {
            // 서비스 로직을 통해 ID/PW 검증
            User loginUser = userService.login(loginDto.getUserId(), loginDto.getPassword());

            // 인증 성공 시 세션 생성 (기존 세션이 있으면 반환, 없으면 신규 생성)
            HttpSession session = request.getSession();
            // 세션에 로그인한 유저의 전체 객체를 담아둡니다.
            session.setAttribute("loginUser", loginUser);

            return ResponseEntity.ok("로그인 성공! 환영합니다, " + loginUser.getNickName() + "님.");
        } catch (IllegalArgumentException e) {
            // 아이디가 없거나 비밀번호가 틀린 경우 400 Bad Request와 에러 메시지 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 3. 로그아웃 (POST /api/users/logout)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // 기존 세션이 존재할 때만 가져오고, 없으면 null 반환 (false 옵션)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 정보를 완전히 만료(삭제)시킵니다.
        }
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}