package com.ssafy.yamyam.domain.recognizedFood.model;

public class RecognizedFood {

	
	private Long recognizedFoodId;
	
	//영상과 매핑
    private Long mealRecordId;
    
    //음식 이름
    private String foodName;
    
    //음식 칼로리
    private Double calories;
    
    //음식 탄수화물
    private Double carbohydrate;
    
    //음식 단백질
    private Double protein;
    
    //음식 지방
    private Double fat;
}
