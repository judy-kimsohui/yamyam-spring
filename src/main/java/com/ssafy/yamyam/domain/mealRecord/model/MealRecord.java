package com.ssafy.yamyam.domain.mealRecord.model;

import java.time.LocalDateTime;

//영상 정보
public class MealRecord {
	
	//영상 아이디
	private String mealRecordId;
	
	//영상을 업로드한 유저 아이디
    private String userId;
    
    //영상 URL
    private String videoUrl;
    
    //영상 설명
    private String description;
    
    //영상 상태
    private String status; // ANALYZING, DONE, FAIL
    
    //생성 일자
    private LocalDateTime createdAt;
    
    //수정 일자
    private LocalDateTime updatedAt;
}
