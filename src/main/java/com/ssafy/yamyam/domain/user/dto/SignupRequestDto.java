package com.ssafy.yamyam.domain.user.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SignupRequestDto {
    private String userId; //로그인용 ID
    private String password; // 비밀번호
    private String nickName; // 닉네임

    private MultipartFile profileImgFile;

    private int age;
    private String gender;
    private double height;
    private double weight;
    private double goalWeight;
}
