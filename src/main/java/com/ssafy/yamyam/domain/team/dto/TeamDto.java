package com.ssafy.yamyam.domain.team.dto;

import lombok.Data;

@Data
public class TeamDto {

    private Long id;            // 시스템 내부 PK (USERS의 id와 매핑용)
    private String inviteCode;  // 초대 링크/초대코드 공유용
    private String name;        // 팀 이름 (프론트엔드의 group.name과 매핑)
    private int members;        // 현재 참여 중인 인원 수 (프론트엔드의 group.members와 매핑)
}
