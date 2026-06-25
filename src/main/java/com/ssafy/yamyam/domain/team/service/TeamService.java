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
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamMapper teamMapper;
    private final UserService userService;

    public TeamService(TeamMapper teamMapper, UserService userService) {
        this.teamMapper = teamMapper;
        this.userService = userService;
    }

    public Map<String, Object> getTeamPreviewByInviteCode(String inviteCode) {
        Team team = teamMapper.findTeamByInviteCode(inviteCode);
        if (team == null) {
            throw new IllegalArgumentException("유효하지 않은 초대 코드입니다.");
        }
        int memberCount = teamMapper.countTeamMembers(team.getId());
        Map<String, Object> preview = new java.util.LinkedHashMap<>();
        preview.put("teamName", team.getTeamName());
        preview.put("memberCount", memberCount);
        preview.put("capacity", team.getCapacity());
        preview.put("inviteCode", inviteCode);
        return preview;
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
    
    @Transactional
    public void leaveTeam(Long loginUserKey, Long teamId) {
        Team team = teamMapper.findTeamById(teamId);
        if (team == null) throw new IllegalArgumentException("존재하지 않는 팀입니다.");

        // 방장은 일반적인 방법으로 나갈 수 없음 (팀을 삭제하거나 방장 권한을 위임해야 함)
        if (team.getKingId().equals(loginUserKey)) {
            throw new IllegalArgumentException("방장은 팀을 나갈 수 없습니다. 팀을 삭제해주세요.");
        }

        int exists = teamMapper.existsTeamMember(teamId, loginUserKey);
        if (exists == 0) throw new IllegalArgumentException("해당 팀의 팀원이 아닙니다.");

        // 멤버 삭제
        teamMapper.deleteTeamMember(teamId, loginUserKey);
    }

    // 2. 팀 삭제 (방장 전용)
    @Transactional
    public void deleteTeam(Long loginUserKey, Long teamId) {
        Team team = teamMapper.findTeamById(teamId);
        if (team == null) throw new IllegalArgumentException("존재하지 않는 팀입니다.");

        // 방장 여부 검증
        if (!team.getKingId().equals(loginUserKey)) {
            throw new SecurityException("방장만 팀을 삭제할 수 있습니다.");
        }

        // DB의 ON DELETE CASCADE 기능에 의해 연관된 팀원(TEAM_MEMBERS)과 영상(VIDEOS)은 자동 삭제됩니다.
        // 애플리케이션 레벨에서는 팀 자체만 삭제하면 됩니다.
        teamMapper.deleteTeam(teamId);
    }

    // 3. 팀원 추방 (방장 전용)
    @Transactional
    public void kickMember(Long loginUserKey, Long teamId, Long targetUserId) {
        Team team = teamMapper.findTeamById(teamId);
        if (team == null) throw new IllegalArgumentException("존재하지 않는 팀입니다.");

        // 방장 여부 검증
        if (!team.getKingId().equals(loginUserKey)) {
            throw new SecurityException("방장만 팀원을 추방할 수 있습니다.");
        }

        // 방장 본인을 추방하려는 경우 방어
        if (team.getKingId().equals(targetUserId)) {
            throw new IllegalArgumentException("방장 본인은 추방할 수 없습니다.");
        }

        int exists = teamMapper.existsTeamMember(teamId, targetUserId);
        if (exists == 0) throw new IllegalArgumentException("해당 유저는 팀원이 아닙니다.");

        // 멤버 추방(삭제)
        teamMapper.deleteTeamMember(teamId, targetUserId);
    }
}