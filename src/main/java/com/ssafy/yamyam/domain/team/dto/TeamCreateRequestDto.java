package com.ssafy.yamyam.domain.team.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamCreateRequestDto {
    @NotBlank(message = "팀 이름은 필수입니다.")
    private String teamName;

    @Min(value = 1, message = "팀 정원은 1명 이상이어야 합니다.")
    private Integer capacity = 10;

    private String teamGoal;
}