package com.ssafy.yamyam.domain.team.service;

import org.springframework.stereotype.Service;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;
import com.ssafy.yamyam.domain.team.mapper.TeamMapper;

@Service
public class TeamService {

    private final TeamMapper teamMapper;

    public TeamService(TeamMapper teamMapper) {
        this.teamMapper = teamMapper;
    }

    public TeamDetailResponseDto getTeamDetail(Long id) {
        TeamDetailResponseDto response = teamMapper.findTeamDetailById(id);
        if (response == null || response.getTeamInfo() == null) {
            throw new RuntimeException("해당 팀을 찾을 수 없습니다. ID: " + id);
        }
        return response;
    }
}