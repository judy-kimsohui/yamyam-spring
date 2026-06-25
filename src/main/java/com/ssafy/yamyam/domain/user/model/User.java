package com.ssafy.yamyam.domain.user.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
	
	//////////////////////////////////////////////////
	// DB의 Auto-Increment PK로 사용할 고유 시스템 ID
    private Long id;
    
    // 사용자가 로그인할 때 사용하는 아이디 (Unique)
    private String userId;
	
	private String password;
	
	private String nickName;
		
	private String profileImg;
	
	//나이
	private int age;
	
	//성별
	private Gender gender;
	
	//현재 키
	private double height;
	
	//현재 몸무게
	private double weight;
	
	//목표 몸무게
	private double goalWeight;
	
	//현재 속해있는 그룹 리스트
	//private List<Group> myGroups;
	/////////////////////////////////////////////////
	private String userGoal;
	
	
	//가입 일자
	private LocalDateTime createdAt;
	
	//수정 일자
	private LocalDateTime updatedAt;
	
	public enum Gender{
		MALE, FEMALE, NONE
	}
	
}
