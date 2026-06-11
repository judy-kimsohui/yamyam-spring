package com.ssafy.yamyam.domain.team.controller;

import com.ssafy.yamyam.domain.team.dto.TeamCreateRequestDto;
import com.ssafy.yamyam.domain.team.dto.TeamDto;
import com.ssafy.yamyam.domain.team.dto.TeamJoinRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;
import com.ssafy.yamyam.domain.team.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTeams(HttpServletRequest request) {
        // 1. 세션에서 로그인한 유저의 시스템 내부 PK(id)를 꺼냅니다.
        Long loginUserKey = (Long) request.getAttribute("loginUserKey");

        // 만약 테스트 중이라 세션이 없다면 임시로 test 계정의 id(예: 1L)를 바인딩
        if (loginUserKey == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        // 2. 서비스 레이어를 호출하여 내가 속한 팀 목록을 조회해 반환
        List<TeamDto> myTeams = teamService.findTeamsByUserId(loginUserKey);
        return ResponseEntity.ok(myTeams);
    }

    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody TeamCreateRequestDto requestDto, HttpServletRequest request) {
        Long loginUserKey = (Long) request.getAttribute("loginUserKey");

        if (loginUserKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        try {
            TeamDetailResponseDto createdTeam = teamService.createTeam(loginUserKey, requestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTeam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinTeam(@Valid @RequestBody TeamJoinRequestDto requestDto, HttpServletRequest request) {
        Long loginUserKey = (Long) request.getAttribute("loginUserKey");

        if (loginUserKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        try {
            TeamDetailResponseDto joinedTeam = teamService.joinTeam(loginUserKey, requestDto);
            return ResponseEntity.ok(joinedTeam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // GET http://localhost:8080/api/teams/1
    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> getTeamDetail(@PathVariable("id") Long id) {
        TeamDetailResponseDto teamDetail = teamService.getTeamDetail(id);
        return ResponseEntity.ok(teamDetail);
    }


}