package com.ssafy.yamyam.domain.team.dto;

import java.util.List;

import com.ssafy.yamyam.domain.team.model.Team;
import com.ssafy.yamyam.domain.user.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDetailResponseDto {
	private Team teamInfo;		//팀 기본 정보
	private User teamKing;		//팀 방장 상세 정보
	private List<User> members;
}
