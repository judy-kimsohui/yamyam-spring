-- ================================================================
-- YamYam DB 마이그레이션 스크립트
-- 실행: docker exec -i yamyam-spring-mysql-1 \
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `USERS` VALUES
(1,'ssafy123','hashed_password_1','김싸피','https://r2.yamyam.com/profiles/kim.png',26,'MALE',175.5,74.2,68,NULL,'2026-06-08 04:20:04','2026-06-08 04:20:04'),
(2,'cheon99','hashed_password_2','천기오','https://r2.yamyam.com/profiles/cheon.png',25,'MALE',180,85.5,78,NULL,'2026-06-08 04:20:04','2026-06-08 04:20:04'),
(3,'yummy_fan','hashed_password_3','밥돌이',NULL,22,'NONE',162,55,53.5,NULL,'2026-06-08 04:20:04','2026-06-08 04:20:04'),
(4,'sohui','sohui','sohui','default_profile.png',26,'FEMALE',159.5,100,50,NULL,'2026-06-08 04:32:47','2026-06-08 04:32:47'),
(5,'test','test','테스트유저',NULL,25,'NONE',170,65,60,NULL,'2026-06-08 07:54:38','2026-06-08 08:06:51');

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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `TEAMS` VALUES
(1,'ssafy-diet-01','SSAFY 1학기 다이어트반',10,'INVITE-ABCD-1234','한 달간 다 같이 -5kg 감량!',1,'2026-06-08 04:20:06','2026-06-08 04:20:06'),
(2,'bulkup-crew','직진 헬스 벌크업 크루',5,'INVITE-HELL-O999','린매스업 및 탄단지 식단 인증',2,'2026-06-08 04:20:06','2026-06-08 04:20:06');

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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `TEAM_MEMBERS` VALUES
(1,1,1,'2026-06-08 04:20:08'),(2,1,2,'2026-06-08 04:20:08'),(3,1,3,'2026-06-08 04:20:08'),
(4,2,2,'2026-06-08 04:20:08'),(5,2,1,'2026-06-08 04:20:08'),
(6,1,4,'2026-06-08 07:54:43'),(7,1,5,'2026-06-08 07:54:43'),
(8,2,4,'2026-06-08 07:54:43'),(9,2,5,'2026-06-08 07:54:43');

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
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `VIDEOS` VALUES
(8, 1,1,'BREAKFAST','2026-06-08','/videos/ssafy_bf.mp4','오트밀 프로틴볼','2026-06-08 08:03:03'),
(9, 1,1,'LUNCH',   '2026-06-08','/videos/ssafy_lu.mp4','닭가슴살 샐러드','2026-06-08 08:03:03'),
(10,1,1,'DINNER',  '2026-06-08','/videos/ssafy_di.mp4','연어 스테이크','2026-06-08 08:03:03'),
(11,2,1,'BREAKFAST','2026-06-08','/videos/cheon_bf.mp4','계란 토스트','2026-06-08 08:03:03'),
(12,2,1,'LUNCH',   '2026-06-08','/videos/cheon_lu.mp4','한식 도시락','2026-06-08 08:03:03'),
(13,3,1,'LUNCH',   '2026-06-08','/videos/yummy_lu.mp4','비빔밥','2026-06-08 08:03:03'),
(14,4,1,'BREAKFAST','2026-06-08','/videos/sohui_bf.mp4','단백질 쉐이크','2026-06-08 08:03:03');

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
