package com.ssafy.yamyam.domain.user.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String userId;   // 로그인할 아이디
    private String password; // 비밀번호
}