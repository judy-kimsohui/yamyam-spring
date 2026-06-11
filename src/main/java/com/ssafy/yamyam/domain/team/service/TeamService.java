package com.ssafy.yamyam.domain.team.service;

import com.ssafy.yamyam.domain.team.dto.TeamCreateRequestDto;
import com.ssafy.yamyam.domain.team.dto.TeamDto;
import com.ssafy.yamyam.domain.team.dto.TeamJoinRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;
import com.ssafy.yamyam.domain.team.mapper.TeamMapper;
import com.ssafy.yamyam.domain.team.model.Team;
import com.ssafy.yamyam.domain.team.model.TeamMember;
import com.ssafy.yamyam.domain.user.model.User;
import com.ssafy.yamyam.domain.user.service.UserService;

import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamMapper teamMapper;
    private final UserService userService;

    public TeamService(TeamMapper teamMapper, UserService userService) {
        this.teamMapper = teamMapper;
        this.userService = userService;
    }

    public TeamDetailResponseDto getTeamDetail(Long id) {
        TeamDetailResponseDto response = teamMapper.findTeamDetailById(id);
        if (response == null || response.getTeamInfo() == null) {
            throw new IllegalArgumentException("해당 팀을 찾을 수 없습니다. ID: " + id);
        }
        return response;
    }

    @Transactional
    public TeamDetailResponseDto createTeam(Long loginUserKey, TeamCreateRequestDto requestDto) {
        if (loginUserKey == null) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }

        if (userService.getUserInfo(loginUserKey) == null) {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다.");
        }

        String teamName = requestDto.getTeamName() == null ? "" : requestDto.getTeamName().trim();
        if (teamName.isEmpty()) {
            throw new IllegalArgumentException("팀 이름은 필수입니다.");
        }

        int capacity = requestDto.getCapacity() == null ? 10 : requestDto.getCapacity();
        if (capacity < 1) {
            throw new IllegalArgumentException("팀 정원은 1명 이상이어야 합니다.");
        }

        Team team = new Team();
        team.setTeamId(generateTeamId(teamName));
        team.setTeamName(teamName);
        team.setCapacity(capacity);
        team.setInviteCode(generateInviteCode());
        team.setTeamGoal(requestDto.getTeamGoal());
        team.setKingId(loginUserKey);

        teamMapper.insertTeam(team);

        TeamMember creatorMember = new TeamMember();
        creatorMember.setTeamId(team.getId());
        creatorMember.setUserId(loginUserKey);
        teamMapper.insertTeamMember(creatorMember);

        return getTeamDetail(team.getId());
    }

    @Transactional
    public TeamDetailResponseDto joinTeam(Long loginUserKey, TeamJoinRequestDto requestDto) {
        if (loginUserKey == null) {
            throw new IllegalArgumentException("인증되지 않은 사용자입니다.");
        }

        String inviteCode = requestDto.getInviteCode() == null ? "" : requestDto.getInviteCode().trim();
        if (inviteCode.isEmpty()) {
            throw new IllegalArgumentException("초대 코드가 필요합니다.");
        }

        Team team = teamMapper.findTeamByInviteCode(inviteCode);
        if (team == null) {
            throw new IllegalArgumentException("유효하지 않은 초대 코드입니다.");
        }

        if (teamMapper.countTeamMembers(team.getId()) >= team.getCapacity()) {
            throw new IllegalArgumentException("이미 팀 정원이 가득 찼습니다.");
        }

        if (teamMapper.existsTeamMember(team.getId(), loginUserKey) > 0) {
            throw new IllegalArgumentException("이미 참여 중인 팀입니다.");
        }

        TeamMember newMember = new TeamMember();
        newMember.setTeamId(team.getId());
        newMember.setUserId(loginUserKey);
        teamMapper.insertTeamMember(newMember);

        return getTeamDetail(team.getId());
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

    private String generateTeamId(String teamName) {
        String normalized = teamName.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9가-힣]+", "-")
                .replaceAll("^-+|-+$", "");
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        if (normalized.isBlank()) {
            return "team-" + suffix;
        }
        return normalized + "-" + suffix;
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase(Locale.ROOT);
    }
}