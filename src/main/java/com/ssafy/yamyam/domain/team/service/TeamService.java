package com.ssafy.yamyam.domain.team.service;

import com.ssafy.yamyam.domain.team.dto.TeamDto;
import org.springframework.stereotype.Service;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;
import com.ssafy.yamyam.domain.team.mapper.TeamMapper;

import java.util.ArrayList;
import java.util.List;

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


    public List<TeamDto> findTeamsByUserId(Long loginUserKey) {
        // 1. 방어 코드: 들어온 유저 키가 올바르지 않으면 빈 리스트를 반환하여 프론트 크래시 방지
        if (loginUserKey == null) {
            return new ArrayList<>();
        }

        // 2. 매퍼(또는 레포지토리)를 호출해 DB 가입 정보를 List로 긁어옵니다.
        List<TeamDto> myTeams = teamMapper.findTeamsByUserId(loginUserKey);

        // 3. 만약 조회된 리스트가 null이라면 안전하게 빈 ArrayList로 감싸서 반환합니다.
        if (myTeams == null) {
            return new ArrayList<>();
        }

        return myTeams;
    }
}