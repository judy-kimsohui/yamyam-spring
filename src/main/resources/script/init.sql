-- 1. 유저 테이블
CREATE TABLE USERS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,          -- 시스템 내부 PK
    user_id VARCHAR(50) NOT NULL UNIQUE,          -- 로그인용 아이디 (중복 불가능)
    password VARCHAR(255) NOT NULL,               -- 암호화된 비밀번호 저장
    nick_name VARCHAR(50) NOT NULL,
    profile_img VARCHAR(512),
    age INT,
    gender VARCHAR(10) DEFAULT 'NONE',            -- MALE, FEMALE, NONE 문자열 매핑
    height DOUBLE DEFAULT 0.0,
    weight DOUBLE DEFAULT 0.0,
    goal_weight DOUBLE DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 그룹 마스터 테이블
CREATE TABLE TEAMS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id VARCHAR(50) NOT NULL UNIQUE,         -- 커스텀 그룹 ID
    team_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL DEFAULT 10,
    invite_code VARCHAR(20) NOT NULL UNIQUE,
    team_goal VARCHAR(255),
    king_id BIGINT NOT NULL,                      -- 방장 유저의 고유 ID (외래키)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (king_id) REFERENCES USERS(id)
);



-- 3. 유저-그룹 다대다(N:M) 매핑 중간 테이블
CREATE TABLE TEAM_MEMBERS (
    team_member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES TEAMS(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_group (team_id, user_id) -- 중복 가입 방지
);