package com.ssafy.yamyam.domain.team.model;

import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.yamyam.domain.user.model.User;
import lombok.Data;

@Data
public class Team {

	
	
	// DB의 Auto-Increment PK로 사용할 고유 시스템 ID
    private Long id;
    
    
	//group ID
	private String teamId;
	
	//그룹 명
	private String teamName;
	
	//그룹 최대 인원 수
	private int capacity;
	
	//초대 코드
	private String inviteCode;
	
	//그룹 목표
	private String teamGoal;
	
	//그룹 참여 User
	//private List<User> members;
	
	//방장의
	private Long kingId;
	
	//생성 일자
	private LocalDateTime createdAt;
	
	//수정 일자
	private LocalDateTime updatedAt;
}
