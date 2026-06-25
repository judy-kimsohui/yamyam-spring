package com.ssafy.yamyam.domain.team.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamJoinRequestDto {
    @NotBlank(message = "초대 코드는 필수입니다.")
    private String inviteCode;
}