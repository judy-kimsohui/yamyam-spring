package com.ssafy.yamyam.domain.user.dto;


import lombok.Data;

@Data
public class UserUpdateRequestDto {
    private String nickName;
    private int age;
    private String gender;
    private double height;
    private double weight;
    private double goalWeight;
}
