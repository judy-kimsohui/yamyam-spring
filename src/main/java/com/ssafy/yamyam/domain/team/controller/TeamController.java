package com.ssafy.yamyam.domain.team.controller;

import com.ssafy.yamyam.domain.team.dto.TeamDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;
import com.ssafy.yamyam.domain.team.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }
    // ⚠️ 반드시 @GetMapping("/{id}") 메서드보다 "위쪽"에 배치하세요!
    @GetMapping("/my")
    public ResponseEntity<?> getMyTeams(HttpSession session) {
        // 1. 세션에서 로그인한 유저의 시스템 내부 PK(id)를 꺼냅니다.
        Long loginUserKey = (Long) session.getAttribute("loginUserKey");

        // 만약 테스트 중이라 세션이 없다면 임시로 test 계정의 id(예: 1L)를 바인딩
        if (loginUserKey == null) {
            loginUserKey = 1L;
        }

        // 2. 서비스 레이어를 호출하여 내가 속한 팀 목록을 조회해 반환합니다.
        List<TeamDto> myTeams = teamService.findTeamsByUserId(loginUserKey);
        return ResponseEntity.ok(myTeams);
    }


        // GET http://localhost:8080/api/teams/1
    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> getTeamDetail(@PathVariable("id") Long id) {
        TeamDetailResponseDto teamDetail = teamService.getTeamDetail(id);
        return ResponseEntity.ok(teamDetail);
    }


}