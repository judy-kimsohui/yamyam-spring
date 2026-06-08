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


ALTER TABLE USERS ADD COLUMN user_goal VARCHAR(255) AFTER goal_weight;


-- 4. 유저 무게 기록 테이블 (💡 FK 연동 구조를 BIGINT id로 안전하게 수정)
CREATE TABLE WEIGHT_HISTORY (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,                      -- 💡 USERS(id)와 매칭되도록 BIGINT로 변경
    recorded_weight DOUBLE NOT NULL,
    recorded_date DATE DEFAULT (CURRENT_DATE),

    CONSTRAINT uq_user_date UNIQUE (user_id, recorded_date),
    CONSTRAINT fk_history_user FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE
);

-- 5. 동영상 업로드 관리 테이블
CREATE TABLE VIDEOS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,                      -- 영상을 업로드한 유저
    team_id BIGINT NOT NULL,                      -- 영상을 공유한 대상 팀
    meal_type VARCHAR(20) NOT NULL DEFAULT 'LUNCH', -- BREAKFAST, LUNCH, DINNER
    meal_date DATE NOT NULL,                      -- 해당 식사 날짜
    video_url VARCHAR(512) NOT NULL,              -- 로컬 파일 서빙 경로
    description VARCHAR(500),                     -- 영상 설명 (선택)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES USERS(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES TEAMS(id) ON DELETE CASCADE,
    UNIQUE KEY uq_user_team_meal (user_id, team_id, meal_type, meal_date)
);