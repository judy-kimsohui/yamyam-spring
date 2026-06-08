package com.ssafy.yamyam.domain.user.dto;


import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String user_id;
    private String nick_name; // 또는 nickName
    private String profile_img;
    private int age;
    private String gender;
    private double height;
    private double weight;
    private double goal_weight;
    private String user_goal;
	
}
