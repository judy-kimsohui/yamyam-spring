-- ================================================================
-- YamYam DB 마이그레이션 스크립트
-- 실행: docker exec -i app-mysql-1 \
--         mysql -uroot -pssafy --default-character-set=utf8mb4 yamyamdb \
--         < migrate.sql
-- ================================================================

/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;
/*!40014 SET UNIQUE_CHECKS=0 */;
/*!40101 SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- ── USERS ────────────────────────────────────────────────────────
DROP TABLE IF EXISTS `USERS`;
CREATE TABLE `USERS` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nick_name` varchar(50) NOT NULL,
  `profile_img` varchar(512) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `gender` varchar(10) DEFAULT 'NONE',
  `height` double DEFAULT '0',
  `weight` double DEFAULT '0',
  `goal_weight` double DEFAULT '0',
  `user_goal` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `USERS` (user_id, password, nick_name, age, gender, height, weight, goal_weight, user_goal) VALUES
('sohui',  'sohui', 'sohui',  26, 'FEMALE', 159.5, 100.0, 50.0, '다이어트'),
('test',   'test',  '테스트유저', 25, 'NONE', 170.0, 65.0, 60.0, NULL),
('jihun',  '1234',  '김지훈',  25, 'MALE',   178.0, 72.0,  68.0, '체중 감량'),
('soyeon', '1234',  '박소연',  23, 'FEMALE', 163.0, 55.0,  52.0, '건강 유지'),
('minjun', '1234',  '이민준',  26, 'MALE',   181.0, 80.0,  75.0, '근육 증량'),
('yuna',   '1234',  '최유나',  24, 'FEMALE', 167.0, 58.0,  55.0, '다이어트'),
('suhyun', '1234',  '정수현',  27, 'MALE',   175.0, 77.0,  72.0, '체중 감량');

-- ── TEAMS ────────────────────────────────────────────────────────
DROP TABLE IF EXISTS `TEAMS`;
CREATE TABLE `TEAMS` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` varchar(50) NOT NULL,
  `team_name` varchar(100) NOT NULL,
  `capacity` int NOT NULL DEFAULT '10',
  `invite_code` varchar(20) NOT NULL,
  `team_goal` varchar(255) DEFAULT NULL,
  `king_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `team_id` (`team_id`),
  UNIQUE KEY `invite_code` (`invite_code`),
  KEY `king_id` (`king_id`),
  CONSTRAINT `TEAMS_ibfk_1` FOREIGN KEY (`king_id`) REFERENCES `USERS` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `TEAMS` (team_id, team_name, capacity, invite_code, team_goal, king_id) VALUES
('health-crew-01', '헬스 크루',       10, 'HLTH-CREW-0001', '매일 운동하고 건강한 식단으로 체력 증진!', (SELECT id FROM USERS WHERE user_id='jihun')),
('diet-challenge',  '다이어트 챌린지', 8,  'DIET-CHAL-0002', '30일 안에 목표 체중 달성하기!',            (SELECT id FROM USERS WHERE user_id='soyeon')),
('morning-club',    '아침 루틴 클럽',  12, 'MORN-CLUB-0003', '건강한 아침 식사로 하루를 시작해요!',       (SELECT id FROM USERS WHERE user_id='suhyun'));

-- ── TEAM_MEMBERS ─────────────────────────────────────────────────
DROP TABLE IF EXISTS `TEAM_MEMBERS`;
CREATE TABLE `TEAM_MEMBERS` (
  `team_member_id` bigint NOT NULL AUTO_INCREMENT,
  `team_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`team_member_id`),
  UNIQUE KEY `unique_user_group` (`team_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `TEAM_MEMBERS_ibfk_1` FOREIGN KEY (`team_id`) REFERENCES `TEAMS` (`id`) ON DELETE CASCADE,
  CONSTRAINT `TEAM_MEMBERS_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 헬스 크루: 김지훈(방장), 이민준, 정수현, sohui
INSERT INTO TEAM_MEMBERS (team_id, user_id)
SELECT t.id, u.id FROM TEAMS t JOIN USERS u ON u.user_id IN ('jihun','minjun','suhyun','sohui')
WHERE t.team_id = 'health-crew-01';

-- 다이어트 챌린지: 박소연(방장), 최유나, 김지훈, sohui
INSERT INTO TEAM_MEMBERS (team_id, user_id)
SELECT t.id, u.id FROM TEAMS t JOIN USERS u ON u.user_id IN ('soyeon','yuna','jihun','sohui')
WHERE t.team_id = 'diet-challenge';

-- 아침 루틴 클럽: 정수현(방장), 5명 전원 + sohui
INSERT INTO TEAM_MEMBERS (team_id, user_id)
SELECT t.id, u.id FROM TEAMS t JOIN USERS u ON u.user_id IN ('jihun','soyeon','minjun','yuna','suhyun','sohui')
WHERE t.team_id = 'morning-club';

-- ── VIDEOS ───────────────────────────────────────────────────────
DROP TABLE IF EXISTS `VIDEOS`;
CREATE TABLE `VIDEOS` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  `meal_type` varchar(20) NOT NULL DEFAULT 'LUNCH',
  `meal_date` date NOT NULL DEFAULT (curdate()),
  `video_url` varchar(512) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_user_team_meal` (`user_id`,`team_id`,`meal_type`,`meal_date`),
  KEY `team_id` (`team_id`),
  CONSTRAINT `VIDEOS_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE,
  CONSTRAINT `VIDEOS_ibfk_2` FOREIGN KEY (`team_id`) REFERENCES `TEAMS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ── WEIGHT_HISTORY ───────────────────────────────────────────────
DROP TABLE IF EXISTS `WEIGHT_HISTORY`;
CREATE TABLE `WEIGHT_HISTORY` (
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `recorded_weight` double NOT NULL,
  `recorded_date` date DEFAULT (curdate()),
  PRIMARY KEY (`history_id`),
  UNIQUE KEY `uq_user_date` (`user_id`,`recorded_date`),
  CONSTRAINT `fk_history_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*!40014 SET FOREIGN_KEY_CHECKS=1 */;
/*!40014 SET UNIQUE_CHECKS=1 */;
