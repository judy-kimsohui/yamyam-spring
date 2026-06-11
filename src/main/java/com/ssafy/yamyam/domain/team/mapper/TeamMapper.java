package com.ssafy.yamyam.domain.team.mapper;

import com.ssafy.yamyam.domain.team.dto.TeamDto;
import com.ssafy.yamyam.domain.team.model.Team;
import com.ssafy.yamyam.domain.team.model.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;

import java.util.List;

@Mapper
public interface TeamMapper {
	
	
	void insertTeam(Team team);
	void insertTeamMember(TeamMember teamMember);
	Team findTeamByInviteCode(@Param("inviteCode") String inviteCode);
	int countTeamMembers(@Param("teamId") Long teamId);
	int existsTeamMember(@Param("teamId") Long teamId, @Param("userId") Long userId);
	TeamDetailResponseDto findTeamDetailById(@Param("id") Long id);
	List<TeamDto> findTeamsByUserId(Long userId);

}
