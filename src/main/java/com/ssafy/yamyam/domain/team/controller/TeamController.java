package com.ssafy.yamyam.domain.team.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;
import com.ssafy.yamyam.domain.team.service.TeamService;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    // GET http://localhost:8080/api/teams/1
    @GetMapping("/{id}")
    public ResponseEntity<TeamDetailResponseDto> getTeamDetail(@PathVariable("id") Long id) {
        TeamDetailResponseDto teamDetail = teamService.getTeamDetail(id);
        return ResponseEntity.ok(teamDetail);
    }
}