package com.ssafy.yamyam.domain.chat.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
    private String id;
    private Long teamId;
    private Long userId;
    private String nickName;
    private String text;
    private String timestamp;   // ISO-8601 문자열로 프론트에 전달
    private boolean mine;       // 수신 측에서 계산 (서버에서는 false 고정)
}
