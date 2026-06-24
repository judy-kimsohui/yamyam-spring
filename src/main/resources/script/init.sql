-- ================================================================
-- yamyam init.sql  (EC2 실제 데이터 기반)
-- 스키마 초기화 + 기본 사용자·팀·멤버 데이터
-- 실행 후 dummy.sql 로 목업 영상·체중 데이터를 추가하세요.
-- ================================================================
SET NAMES utf8mb4;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

-- ----------------------------------------------------------------
-- 1. 기존 테이블 완전 삭제
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS `VIDEO_LIKES`;
DROP TABLE IF EXISTS `RECOGNIZED_FOOD_ITEM`;
DROP TABLE IF EXISTS `NUTRITION_ANALYSIS`;
DROP TABLE IF EXISTS `WEIGHT_HISTORY`;
DROP TABLE IF EXISTS `VIDEOS`;
DROP TABLE IF EXISTS `TEAM_MEMBERS`;
DROP TABLE IF EXISTS `TEAMS`;
DROP TABLE IF EXISTS `USERS`;

-- ----------------------------------------------------------------
-- 2. 스키마 생성 (EC2 실제 스키마)
-- ----------------------------------------------------------------

CREATE TABLE `USERS` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `user_id`      varchar(50)  NOT NULL,
  `password`     varchar(255) NOT NULL,
  `nick_name`    varchar(50)  NOT NULL,
  `profile_img`  varchar(512) DEFAULT NULL,
  `age`          int          DEFAULT NULL,
  `gender`       varchar(10)  DEFAULT 'NONE',
  `height`       double       DEFAULT '0',
  `weight`       double       DEFAULT '0',
  `goal_weight`  double       DEFAULT '0',
  `user_goal`    varchar(255) DEFAULT NULL,
  `created_at`   timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   timestamp    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `TEAMS` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `team_id`     varchar(50)  NOT NULL,
  `team_name`   varchar(100) NOT NULL,
  `capacity`    int          NOT NULL DEFAULT '10',
  `invite_code` varchar(20)  NOT NULL,
  `team_goal`   varchar(255) DEFAULT NULL,
  `king_id`     bigint       NOT NULL,
  `created_at`  timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  timestamp    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `team_id`     (`team_id`),
  UNIQUE KEY `invite_code` (`invite_code`),
  KEY `king_id` (`king_id`),
  CONSTRAINT `TEAMS_ibfk_1` FOREIGN KEY (`king_id`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `TEAM_MEMBERS` (
  `team_member_id` bigint    NOT NULL AUTO_INCREMENT,
  `team_id`        bigint    NOT NULL,
  `user_id`        bigint    NOT NULL,
  `joined_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`team_member_id`),
  UNIQUE KEY `unique_user_group` (`team_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `TEAM_MEMBERS_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `TEAMS` (`id`) ON DELETE CASCADE,
  CONSTRAINT `TEAM_MEMBERS_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `VIDEOS` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `user_id`     bigint       NOT NULL,
  `team_id`     bigint       NOT NULL,
  `meal_type`   varchar(20)  NOT NULL DEFAULT 'LUNCH',
  `meal_date`   date         NOT NULL DEFAULT (curdate()),
  `video_url`   varchar(512) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_at`  timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_team_meal` (`user_id`,`team_id`,`meal_type`,`meal_date`),
  KEY `team_id` (`team_id`),
  CONSTRAINT `VIDEOS_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE,
  CONSTRAINT `VIDEOS_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `TEAMS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `NUTRITION_ANALYSIS` (
  `id`             bigint      NOT NULL AUTO_INCREMENT,
  `video_id`       bigint      NOT NULL,
  `status`         varchar(20) NOT NULL DEFAULT 'PENDING',
  `total_calories` double      DEFAULT '0',
  `total_carbs`    double      DEFAULT '0',
  `total_protein`  double      DEFAULT '0',
  `total_fat`      double      DEFAULT '0',
  `foods_json`     text,
  `error_message`  text,
  `created_at`     timestamp   NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     timestamp   NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_analysis_video_id` (`video_id`),
  CONSTRAINT `fk_analysis_video` FOREIGN KEY (`video_id`) REFERENCES `VIDEOS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `RECOGNIZED_FOOD_ITEM` (
  `id`                   bigint      NOT NULL AUTO_INCREMENT,
  `nutrition_analysis_id` bigint     NOT NULL,
  `food_name`            varchar(100) NOT NULL,
  `calories`             double      DEFAULT '0',
  `carbs`                double      DEFAULT '0',
  `protein`              double      DEFAULT '0',
  `fat`                  double      DEFAULT '0',
  `quantity`             int         DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `nutrition_analysis_id` (`nutrition_analysis_id`),
  CONSTRAINT `fk_food_item_analysis_id` FOREIGN KEY (`nutrition_analysis_id`) REFERENCES `NUTRITION_ANALYSIS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `VIDEO_LIKES` (
  `video_id` bigint NOT NULL,
  `user_id`  bigint NOT NULL,
  PRIMARY KEY (`video_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `VIDEO_LIKES_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `VIDEOS` (`id`) ON DELETE CASCADE,
  CONSTRAINT `VIDEO_LIKES_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `WEIGHT_HISTORY` (
  `history_id`      bigint NOT NULL AUTO_INCREMENT,
  `user_id`         bigint NOT NULL,
  `recorded_weight` double NOT NULL,
  `recorded_date`   date   DEFAULT (curdate()),
  PRIMARY KEY (`history_id`),
  UNIQUE KEY `uq_user_date` (`user_id`,`recorded_date`),
  CONSTRAINT `fk_history_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;

-- ----------------------------------------------------------------
-- 3. USERS — 20명 (EC2 실제 데이터)
-- ----------------------------------------------------------------
INSERT INTO `USERS`
  (`id`,`user_id`,`password`,`nick_name`,`profile_img`,`age`,`gender`,`height`,`weight`,`goal_weight`,`user_goal`)
VALUES
  ( 1,'ssafy1', 'ssafy1', '김싸피', NULL,26,'MALE',  175,75,70,'식단 조절하고 체중 감량 도전'),
  ( 2,'ssafy2', 'ssafy2', '이하나', NULL,24,'FEMALE',162,56,50,'체력 기르고 건강한 아침 챙기기'),
  ( 3,'ssafy3', 'ssafy3', '박민수', NULL,28,'MALE',  180,82,75,'벌크업을 위한 고단백 식단 정착'),
  ( 4,'ssafy4', 'ssafy4', '최소윤', NULL,25,'FEMALE',165,60,53,'당독소 줄이고 야식 절대 금지'),
  ( 5,'ssafy5', 'ssafy5', '정재형', NULL,27,'MALE',  172,78,72,'내장지방 컷팅 및 유산소 연계'),
  ( 6,'ssafy6', 'ssafy6', '한지민', NULL,23,'FEMALE',158,52,48,'바디프로필 촬영용 단기 감량'),
  ( 7,'ssafy7', 'ssafy7', '강동우', NULL,29,'MALE',  177,85,78,'정제탄수화물 끊기 챌린지 수행'),
  ( 8,'ssafy8', 'ssafy8', '윤서연', NULL,31,'FEMALE',160,55,50,'주 3회 클린 샐러드 식단 유지'),
  ( 9,'ssafy9', 'ssafy9', '조현우', NULL,26,'MALE',  183,90,82,'린매스업 골격근량 올리기'),
  (10,'ssafy10','ssafy10','임수아', NULL,24,'FEMALE',164,58,52,'과자랑 초콜릿 간식 줄이기'),
  (11,'ssafy11','ssafy11','장진우', NULL,25,'MALE',  170,73,68,'복부 비만 탈출 뱃살 빼기'),
  (12,'ssafy12','ssafy12','오지현', NULL,27,'FEMALE',161,54,49,'요가와 병행하는 필라테스 식단'),
  (13,'ssafy13','ssafy13','송민재', NULL,22,'MALE',  176,68,65,'마른 체형 탈출 규칙적인 식사'),
  (14,'ssafy14','ssafy14','신예은', NULL,30,'FEMALE',167,62,55,'붓기 제거 및 저염식 습관 들이기'),
  (15,'ssafy15','ssafy15','고태환', NULL,28,'MALE',  179,80,74,'간헐적 단식 공복 시간 준수'),
  (16,'ssafy16','ssafy16','배유진', NULL,23,'FEMALE',159,50,46,'탄단지 균형 잡힌 정석 식단'),
  (17,'ssafy17','ssafy17','서준호', NULL,33,'MALE',  174,77,70,'성인병 예방 및 저당 음료 대체'),
  (18,'ssafy18','ssafy18','황보라', NULL,26,'FEMALE',168,57,51,'결혼 준비 드레스 라인 관리'),
  (19,'ssafy19','ssafy19','전태양', NULL,29,'MALE',  181,84,77,'크로스핏 수행 능력 칼로리 보충'),
  (20,'ssafy20','ssafy20','문소리', NULL,35,'FEMALE',155,48,45,'회사 점심 건강 도시락 인증');

-- ----------------------------------------------------------------
-- 4. TEAMS — 6개 (EC2 실제 데이터)
-- ----------------------------------------------------------------
INSERT INTO `TEAMS`
  (`id`,`team_id`,`team_name`,`capacity`,`invite_code`,`team_goal`,`king_id`)
VALUES
  (1,'ssafy-team-1','SSAFY 아침 식단 인증반',  10,'INVITE-AAAA-1111','아침 거르지 않고 무조건 먹기',       1),
  (2,'ssafy-team-2','득근득근 헬스 벌크업 크루',10,'INVITE-BBBB-2222','매일 근비대용 단백질 인증',          4),
  (3,'ssafy-team-3','클린 키토 다이어트방',     10,'INVITE-CCCC-3333','당류 제로 정제탄수 금지',             7),
  (4,'ssafy-team-4','바디프로필 컷팅 속성반',   10,'INVITE-DDDD-4444','눈바디 체지방률 감량 달성',          10),
  (5,'ssafy-team-5','직장인 다이어트 도시락파', 10,'INVITE-EEEE-5555','주 3회 직접 싼 메뉴 인증',          13),
  (6,'ssafy-team-6','백세건강 혈당 조절방',     10,'INVITE-FFFF-6666','식후 가벼운 산책 및 저염식',        16);

-- ----------------------------------------------------------------
-- 5. TEAM_MEMBERS — 34개 (EC2 실제 데이터)
-- ----------------------------------------------------------------
INSERT INTO `TEAM_MEMBERS` (`team_id`,`user_id`)
VALUES
  -- team 1: 아침 식단 인증반 (users 1~9)
  (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(1,9),
  -- team 2: 벌크업 크루 (4,10,11,1)
  (2,4),(2,10),(2,11),(2,1),
  -- team 3: 키토 다이어트방 (7,1,12,13,14,4)
  (3,7),(3,1),(3,12),(3,13),(3,14),(3,4),
  -- team 4: 바디프로필 속성반 (10,1,15,16,11)
  (4,10),(4,1),(4,15),(4,16),(4,11),
  -- team 5: 도시락파 (13,17,1)
  (5,13),(5,17),(5,1),
  -- team 6: 혈당 조절방 (16,2,18,19,20,7,5)
  (6,16),(6,2),(6,18),(6,19),(6,20),(6,7),(6,5);

-- NUTRITION_ANALYSIS 테이블에 retry_count 컬럼 추가 (기본값 0, NULL 허용 안 함)
ALTER TABLE NUTRITION_ANALYSIS
    ADD COLUMN retry_count INT NOT NULL DEFAULT 0;


CREATE TABLE daily_ai_comment (
    user_id BIGINT NOT NULL,
    record_date VARCHAR(10) NOT NULL, -- 예: '2026-06-24'
    ai_comment TEXT NOT NULL,
    PRIMARY KEY (user_id, record_date)
);