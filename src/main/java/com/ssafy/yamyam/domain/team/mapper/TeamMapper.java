package com.ssafy.yamyam.domain.team.mapper;

import com.ssafy.yamyam.domain.team.dto.TeamDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;

import java.util.List;

@Mapper
public interface TeamMapper {
	
	
	TeamDetailResponseDto findTeamDetailById(@Param("id") Long id);
	List<TeamDto> findTeamsByUserId(Long userId);

}
