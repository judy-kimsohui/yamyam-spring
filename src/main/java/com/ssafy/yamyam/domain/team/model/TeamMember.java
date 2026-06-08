package com.ssafy.yamyam.domain.team.model;

import java.time.LocalDateTime;

import lombok.Data;


@Data
public class TeamMember {
	
	private Long teamMemberId;
    
	private Long teamId;           // 어떤 그룹에
    
	private Long userId;            // 어떤 유저가 속해있는지 숫자 PK로만 관리
    
	private LocalDateTime joinedAt; //언제 이 유저가 이 그룹에 참여했나?
}
