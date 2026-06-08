package com.ssafy.yamyam.domain.team.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yamyam.domain.team.dto.TeamDetailResponseDto;

@Mapper
public interface TeamMapper {
	
	
	TeamDetailResponseDto findTeamDetailById(@Param("id") Long id); 
}
