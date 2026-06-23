# 📝 YamYam 서비스 데이터베이스 설계서 (DB Specification)

본 문서는 YamYam 프로젝트의 초기 마이그레이션 스크립트(`migrate.sql`)를 기반으로 작성된 데이터베이스 구조 및 테이블 명세서입니다.

---

## 🗺️ 테이블 관계도 요약 (ERD 구조)
* **USERS** (사용자 기본 정보)
    * └── **TEAMS** (팀 정보, `king_id` 참조)
    * └── **TEAM_MEMBERS** (팀-유저 다대다 매핑 테이블)
    * └── **WEIGHT_HISTORY** (체중 기록 일지)
    * └── **VIDEOS** (식단 인증 영상, `user_id` 및 `team_id` 참조)
        * └── **NUTRITION_ANALYSIS** (AI 영양 분석 결과, `video_id` 참조)
            * └── **RECOGNIZED_FOOD_ITEM** (분석된 세부 음식 아이템 리스트)

---

## 🗂️ 테이블 세부 명세서

### 1. USERS (사용자 테이블)
서비스를 이용하는 사용자의 개인 정보 및 다이어트/벌크업 목표를 관리합니다.
* **Engine**: InnoDB | **Charset**: utf8mb4
* **특이사항**: `user_id`는 고유값(Unique) 제약조건이 걸려 있습니다.

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** (PK) | bigint | NOT NULL, AI | 내부 고유 식별자 고유 ID |
| **user_id** | varchar(50) | NOT NULL, UNIQUE | 사용자 로그인 아이디 |
| **password** | varchar(255) | NOT NULL | 암호화된 비밀번호 |
| **nick_name** | varchar(50) | NOT NULL | 사용자 닉네임 |
| **profile_img** | varchar(512) | DEFAULT NULL | 프로필 이미지 URL 경로 |
| **age** | int | DEFAULT NULL | 나이 |
| **gender** | varchar(10) | DEFAULT 'NONE' | 성별 (`MALE`, `FEMALE`, `NONE`) |
| **height** | double | DEFAULT 0 | 키 (cm) |
| **weight** | double | DEFAULT 0 | 현재 체중 (kg) |
| **goal_weight** | double | DEFAULT 0 | 목표 체중 (kg) |
| **user_goal** | varchar(255) | DEFAULT NULL | 개인 다이어트/운동 목표 다짐 |
| **created_at** | timestamp | DEFAULT CURRENT_TIMESTAMP | 계정 생성일 |
| **updated_at** | timestamp | ON UPDATE CURRENT_TIMESTAMP | 계정 정보 수정일 |

* **초기 마이그레이션 데이터 (Seed Data)**
  * `id=1`: 김싸피 (MALE, 175.5cm, 74.2kg -> 목표 68kg)
  * `id=2`: 천기오 (MALE, 180cm, 85.5kg -> 목표 78kg)
  * `id=3`: 밥돌이 (NONE, 162cm, 55kg -> 목표 53.5kg)
  * `id=4`: sohui (FEMALE, 159.5cm, 100kg -> 목표 50kg)
  * `id=5`: 테스트유저 (NONE, 170cm, 65kg -> 목표 60kg)

---

### 2. TEAMS (그룹/팀 테이블)
공동 식단 인증 및 다이어트를 진행하는 그룹 방 정보입니다.
* **Constraint**: `king_id`는 방장 역할을 하는 `USERS.id`를 참조하는 외래키입니다.

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** (PK) | bigint | NOT NULL, AI | 팀 고유 식별자 ID |
| **team_id** | varchar(50) | NOT NULL, UNIQUE | 팀 커스텀 식별 ID |
| **team_name** | varchar(100) | NOT NULL | 팀 이름 (방 제목) |
| **capacity** | int | DEFAULT 10 | 팀 최대 수용 인원 |
| **invite_code** | varchar(20) | NOT NULL, UNIQUE | 팀 입장 초대 코드 |
| **team_goal** | varchar(255) | DEFAULT NULL | 팀 공동 목표 내용 |
| **king_id** (FK) | bigint | NOT NULL | 방장 고유 ID (`USERS.id`) |
| **created_at** | timestamp | DEFAULT CURRENT_TIMESTAMP | 팀 생성일 |
| **updated_at** | timestamp | ON UPDATE CURRENT_TIMESTAMP | 팀 정보 수정일 |

* **초기 마이그레이션 데이터 (Seed Data)**
  * `id=1`: SSAFY 1학기 다이어트반 (방장: 김싸피, 목표: 한 달간 다 같이 -5kg 감량!)
  * `id=2`: 직진 헬스 벌크업 크루 (방장: 천기오, 목표: 린매스업 및 탄단지 식단 인증)

---

### 3. TEAM_MEMBERS (팀 멤버 매핑 테이블)
사용자와 팀 간의 다대다(N:M) 관계를 매핑하는 교차 테이블입니다.
* **Constraint**: `(team_id, user_id)` 복합 유니크 키가 걸려있어 한 팀에 중복 가입을 방지합니다. 외래키에는 `ON DELETE CASCADE` 옵션이 붙어있어 유저나 팀이 삭제되면 멤버 정보도 연쇄 소거됩니다.

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **team_member_id** (PK) | bigint | NOT NULL, AI | 매핑 고유 식별자 ID |
| **team_id** (FK) | bigint | NOT NULL | 소속 팀 ID (`TEAMS.id`) |
| **user_id** (FK) | bigint | NOT NULL | 참여한 유저 ID (`USERS.id`) |
| **joined_at** | timestamp | DEFAULT CURRENT_TIMESTAMP | 팀 가입 시점 일시 |

---

### 4. VIDEOS (식단 인증 영상 테이블)
유저가 팀에 일별 식단을 인증하기 위해 업로드한 비디오 메타데이터입니다.
* **Constraint**: `(user_id, team_id, meal_type, meal_date)`에 복합 유니크 제약조건(`uq_user_team_meal`)이 매핑되어 있어, **동일한 유저가 특정 날짜의 동일한 식사 형태(예: 2026-06-08 점심)에는 오직 하나의 영상만 등록**할 수 있도록 정밀하게 제한합니다.

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** (PK) | bigint | NOT NULL, AI | 비디오 고유 식별자 ID |
| **user_id** (FK) | bigint | NOT NULL | 업로드한 유저 ID (`USERS.id`) |
| **team_id** (FK) | bigint | NOT NULL | 인증을 올린 팀 ID (`TEAMS.id`) |
| **meal_type** | varchar(20) | DEFAULT 'LUNCH' | 식사 종류 (`BREAKFAST`, `LUNCH`, `DINNER` 등) |
| **meal_date** | date | DEFAULT (curdate()) | 식단 기록 대상 날짜 (기본 오늘) |
| **video_url** | varchar(512) | NOT NULL | S3 스토리지에 저장된 실제 영상 경로 |
| **description** | varchar(500) | DEFAULT NULL | 유저가 남긴 식단 한줄 요약/설명 |
| **created_at** | timestamp | DEFAULT CURRENT_TIMESTAMP | 업로드 일시 |

---

### 5. WEIGHT_HISTORY (체중 변화 이력 테이블)
사용자의 날짜별 몸무게 기록을 추적하는 일지 테이블입니다.
* **Constraint**: `(user_id, recorded_date)` 복합 유니크 키로 인해 하루에 한 번만 체중을 기록할 수 있습니다.

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **history_id** (PK) | bigint | NOT NULL, AI | 체중 기록 고유 이력 ID |
| **user_id** (FK) | bigint | NOT NULL | 기록한 유저 ID (`USERS.id`) |
| **recorded_weight** | double | NOT NULL | 기록된 몸무게 수치 (kg) |
| **recorded_date** | date | DEFAULT (curdate()) | 기록 일자 (기본 오늘) |

---

### 6. NUTRITION_ANALYSIS (AI 영양소 식별 메인 테이블)
업로드된 영상을 분석 파이프라인(OpenCV -> GPT-4o)에 태워 도출해낸 메인 영양 분석 레포트 결과입니다.
* **Constraint**: `video_id`에 유니크 키가 부여되어 있어 비디오당 하나의 분석 결과가 보장됩니다. `VIDEOS` 테이블 삭제 시 분석 결과도 연쇄 소거(`CASCADE`)됩니다.

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** (PK) | bigint | NOT NULL, AI | 분석서 고유 식별 ID |
| **video_id** (FK) | bigint | NOT NULL, UNIQUE | 분석 대상 비디오 ID (`VIDEOS.id`) |
| **status** | varchar(20) | DEFAULT 'PENDING' | 분석 진행 상태 (`PENDING`, `DONE`, `FAILED`) |
| **total_calories** | double | DEFAULT 0 | 식단에 포함된 총 칼로리 합산 (kcal) |
| **total_carbs** | double | DEFAULT 0 | 총 탄수화물 함량 합산 (g) |
| **total_protein** | double | DEFAULT 0 | 총 단백질 함량 합산 (g) |
| **total_fat** | double | DEFAULT 0 | 총 지방 함량 합산 (g) |
| **foods_json** | text | DEFAULT NULL | AI가 응답한 가공 전 순수 JSON 데이터 문자열 |
| **error_message** | text | DEFAULT NULL | API 호출 실패 시 원인 추적용 로그 메시지 |
| **created_at** | timestamp | DEFAULT CURRENT_TIMESTAMP | 분석 요청 생성 시점 |
| **updated_at** | timestamp | ON UPDATE CURRENT_TIMESTAMP | 상태 및 결과 최종 매핑 수정 시점 |

---

### 7. RECOGNIZED_FOOD_ITEM (상세 식 식별 음식 아이템 테이블)
AI 영양소 레포트(`NUTRITION_ANALYSIS`)에 포착된 개별 음식 품목들의 상세 리스트 목록입니다.
* **Constraint**: `nutrition_analysis_id`를 참조하며 부모 분석서 삭제 시 함께 연쇄 삭제(`CASCADE`)됩니다.

| 컬럼명 | 데이터 타입 | 제약 조건 | 설명 |
| :--- | :--- | :--- | :--- |
| **id** (PK) | bigint | NOT NULL, AI | 개별 음식 품목 식별 ID |
| **nutrition_analysis_id** (FK) | bigint | NOT NULL | 소속된 영양소 레포트 메인 ID |
| **food_name** | varchar(100) | NOT NULL | AI가 판별한 한글 음식 및 브랜드명 |
| **calories** | double | DEFAULT 0 | 해당 품목 개별 칼로리 |
| **carbs** | double | DEFAULT 0 | 해당 품목 개별 탄수화물 |
| **protein** | double | DEFAULT 0 | 해당 품목 개별 단백질 |
| **fat** | double | DEFAULT 0 | 해당 품목 개별 지방 |
| **quantity** | int | DEFAULT 1 | 식별된 음식 수량/개수 |

---

## 🔒 데이터 정합성 보장 정책 (DB 규칙)
1. **모든 외래키 연쇄 적용**: 주요 데이터 도메인 구조인 `USERS ➔ TEAMS ➔ VIDEOS ➔ NUTRITION_ANALYSIS ➔ RECOGNIZED_FOOD_ITEM` 흐름 전반에 `ON DELETE CASCADE` 정책을 바인딩하여, 회원이 탈퇴하거나 영상이 지워지면 하위 데이터 파편이 남아 스토리지 누수가 발생하는 현상을 데이터베이스 계층에서 완벽히 차단합니다.
2. **복합 인덱스 활용**: `VIDEOS`와 `TEAM_MEMBERS` 테이블 등에 실무 레벨의 복합 유니크 키 규칙을 배치하여 비즈니스 로직 단위의 중복 유입을 원천 봉쇄합니다.




## init/dummy.sql
```sql

-- 1. 환경 설정 및 인코딩 강제 동기화 (한글 유실 차단)
SET NAMES utf8mb4;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

-- 2. 기존 테이블 존재 시 참조 의존성 역순으로 완전 삭제 (Drop)
DROP TABLE IF EXISTS `RECOGNIZED_FOOD_ITEM`;
DROP TABLE IF EXISTS `NUTRITION_ANALYSIS`;
DROP TABLE IF EXISTS `WEIGHT_HISTORY`;
DROP TABLE IF EXISTS `VIDEOS`;
DROP TABLE IF EXISTS `TEAM_MEMBERS`;
DROP TABLE IF EXISTS `TEAMS`;
DROP TABLE IF EXISTS `USERS`;

-- =========================================================================
-- [CREATE] 3. 원본 명세 기반 테이블 스키마 생성 (DDL)
-- =========================================================================

-- 3-1. USERS
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3-2. TEAMS
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3-3. TEAM_MEMBERS
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3-4. VIDEOS
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3-5. WEIGHT_HISTORY
CREATE TABLE `WEIGHT_HISTORY` (
  `history_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `recorded_weight` double NOT NULL,
  `recorded_date` date DEFAULT (curdate()),
  PRIMARY KEY (`history_id`),
  UNIQUE KEY `uq_user_date` (`user_id`,`recorded_date`),
  CONSTRAINT `fk_history_user` FOREIGN KEY (`user_id`) REFERENCES `USERS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3-6. NUTRITION_ANALYSIS
CREATE TABLE `NUTRITION_ANALYSIS` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `video_id` bigint NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `total_calories` double DEFAULT '0',
  `total_carbs` double DEFAULT '0',
  `total_protein` double DEFAULT '0',
  `total_fat` double DEFAULT '0',
  `foods_json` text,
  `error_message` text,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_analysis_video_id` (`video_id`),
  CONSTRAINT `fk_analysis_video` FOREIGN KEY (`video_id`) REFERENCES `VIDEOS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3-7. RECOGNIZED_FOOD_ITEM
CREATE TABLE `RECOGNIZED_FOOD_ITEM` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nutrition_analysis_id` bigint NOT NULL,
  `food_name` varchar(100) NOT NULL,
  `calories` double DEFAULT '0',
  `carbs` double DEFAULT '0',
  `protein` double DEFAULT '0',
  `fat` double DEFAULT '0',
  `quantity` int DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `nutrition_analysis_id` (`nutrition_analysis_id`),
  CONSTRAINT `fk_food_item_analysis_id` FOREIGN KEY (`nutrition_analysis_id`) REFERENCES `NUTRITION_ANALYSIS` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 외래키 잠금 원상 해제
SET FOREIGN_KEY_CHECKS = 1;


-- =========================================================================
-- [INSERT] 4. 실전형 데이터 마이그레이션 주입 (DML)
-- =========================================================================

-- 4-1. 1~20번 번호 일치 유저 주입
INSERT INTO `USERS` (`id`, `user_id`, `password`, `nick_name`, `profile_img`, `age`, `gender`, `height`, `weight`, `goal_weight`, `user_goal`, `created_at`, `updated_at`) VALUES
(1, 'ssafy1', 'ssafy1', '김싸피', NULL, 26, 'MALE', 175.0, 75.0, 70.0, '식단 조절하고 체중 감량 도전', NOW(), NOW()),
(2, 'ssafy2', 'ssafy2', '이하나', NULL, 24, 'FEMALE', 162.0, 56.0, 50.0, '체력 기르고 건강한 아침 챙기기', NOW(), NOW()),
(3, 'ssafy3', 'ssafy3', '박민수', NULL, 28, 'MALE', 180.0, 82.0, 75.0, '벌크업을 위한 고단백 식단 정착', NOW(), NOW()),
(4, 'ssafy4', 'ssafy4', '최소윤', NULL, 25, 'FEMALE', 165.0, 60.0, 53.0, '당독소 줄이고 야식 절대 금지', NOW(), NOW()),
(5, 'ssafy5', 'ssafy5', '정재형', NULL, 27, 'MALE', 172.0, 78.0, 72.0, '내장지방 컷팅 및 유산소 연계', NOW(), NOW()),
(6, 'ssafy6', 'ssafy6', '한지민', NULL, 23, 'FEMALE', 158.0, 52.0, 48.0, '바디프로필 촬영용 단기 감량', NOW(), NOW()),
(7, 'ssafy7', 'ssafy7', '강동우', NULL, 29, 'MALE', 177.0, 85.0, 78.0, '정제탄수화물 끊기 챌린지 수행', NOW(), NOW()),
(8, 'ssafy8', 'ssafy8', '윤서연', NULL, 31, 'FEMALE', 160.0, 55.0, 50.0, '주 3회 클린 샐러드 식단 유지', NOW(), NOW()),
(9, 'ssafy9', 'ssafy9', '조현우', NULL, 26, 'MALE', 183.0, 90.0, 82.0, '린매스업 골격근량 올리기', NOW(), NOW()),
(10, 'ssafy10', 'ssafy10', '임수아', NULL, 24, 'FEMALE', 164.0, 58.0, 52.0, '과자랑 초콜릿 간식 줄이기', NOW(), NOW()),
(11, 'ssafy11', 'ssafy11', '장진우', NULL, 25, 'MALE', 170.0, 73.0, 68.0, '복부 비만 탈출 뱃살 빼기', NOW(), NOW()),
(12, 'ssafy12', 'ssafy12', '오지현', NULL, 27, 'FEMALE', 161.0, 54.0, 49.0, '요가와 병행하는 필라테스 식단', NOW(), NOW()),
(13, 'ssafy13', 'ssafy13', '송민재', NULL, 22, 'MALE', 176.0, 68.0, 65.0, '마른 체형 탈출 규칙적인 식사', NOW(), NOW()),
(14, 'ssafy14', 'ssafy14', '신예은', NULL, 30, 'FEMALE', 167.0, 62.0, 55.0, '붓기 제거 및 저염식 습관 들이기', NOW(), NOW()),
(15, 'ssafy15', 'ssafy15', '고태환', NULL, 28, 'MALE', 179.0, 80.0, 74.0, '간철적 단식 공복 시간 준수', NOW(), NOW()),
(16, 'ssafy16', 'ssafy16', '배유진', NULL, 23, 'FEMALE', 159.0, 50.0, 46.0, '탄단지 균형 잡힌 정석 식단', NOW(), NOW()),
(17, 'ssafy17', 'ssafy17', '서준호', NULL, 33, 'MALE', 174.0, 77.0, 70.0, '성인병 예방 및 저당 음료 대체', NOW(), NOW()),
(18, 'ssafy18', 'ssafy18', '황보라', NULL, 26, 'FEMALE', 168.0, 57.0, 51.0, '결혼 준비 드레스 라인 관리', NOW(), NOW()),
(19, 'ssafy19', 'ssafy19', '전태양', NULL, 29, 'MALE', 181.0, 84.0, 77.0, '크로스핏 수행 능력 칼로리 보충', NOW(), NOW()),
(20, 'ssafy20', 'ssafy20', '문소리', NULL, 35, 'FEMALE', 155.0, 48.0, 45.0, '회사 점심 건강 도시락 인증', NOW(), NOW());

-- 4-2. 6개 얌얌 그룹방 주입
INSERT INTO `TEAMS` (`id`, `team_id`, `team_name`, `capacity`, `invite_code`, `team_goal`, `king_id`, `created_at`, `updated_at`) VALUES
(1, 'ssafy-team-1', 'SSAFY 아침 식단 인증반', 10, 'INVITE-AAAA-1111', '아침 거르지 않고 무조건 먹기', 1, NOW(), NOW()),
(2, 'ssafy-team-2', '득근득근 헬스 벌크업 크루', 10, 'INVITE-BBBB-2222', '매일 근비대용 단백질 인증', 4, NOW(), NOW()),
(3, 'ssafy-team-3', '클린 키토 다이어트방', 10, 'INVITE-CCCC-3333', '당류 제로 정제탄수 금지', 7, NOW(), NOW()),
(4, 'ssafy-team-4', '바디프로필 컷팅 속성반', 10, 'INVITE-DDDD-4444', '눈바디 체지방률 감량 달성', 10, NOW(), NOW()),
(5, 'ssafy-team-5', '직장인 다이어트 도시락파', 10, 'INVITE-EEEE-5555', '주 3회 직접 싼 메뉴 인증', 13, NOW(), NOW()),
(6, 'ssafy-team-6', '백세건강 혈당 조절방', 10, 'INVITE-FFFF-6666', '식후 가벼운 산책 및 저염식', 16, NOW(), NOW());

-- 4-3. 불균형 분산 편차 가중 매핑 멤버십 데이터 주입
INSERT INTO `TEAM_MEMBERS` (`team_id`, `user_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
(2, 4), (2, 10), (2, 11),
(3, 7), (3, 1), (3, 12), (3, 13), (3, 14),
(4, 10), (4, 1), (4, 15), (4, 16),
(5, 13), (5, 17),
(6, 16), (6, 2), (6, 18), (6, 19), (6, 20),
(2, 1), (5, 1), (3, 4), (6, 7), (4, 11);

-- 4-4. WEIGHT_HISTORY (몸무게 타임라인 더미 정보)
INSERT INTO `WEIGHT_HISTORY` (`user_id`, `recorded_weight`, `recorded_date`) VALUES
(1, 75.4, SUBDATE(CURDATE(), INTERVAL 2 DAY)),
(1, 75.1, SUBDATE(CURDATE(), INTERVAL 1 DAY)),
(1, 175.0, CURDATE()),
(2, 56.0, CURDATE());

-- 4-5. VIDEOS (테스트 연계용 비디오 로그 데이터)
INSERT INTO `VIDEOS` (`id`, `user_id`, `team_id`, `meal_type`, `meal_date`, `video_url`, `description`) VALUES
(1, 1, 1, 'BREAKFAST', CURDATE(), '/videos/ssafy_test_breakfast.mp4', '오늘 아침 한식 정석 식단 검증용'),
(2, 2, 1, 'LUNCH', CURDATE(), '/videos/ssafy_test_lunch.mp4', '다이어트 샐러드 팩 섭취');

-- 4-6. NUTRITION_ANALYSIS (동영상 매핑 결과 분석 레포트 연동 더미)
INSERT INTO `NUTRITION_ANALYSIS` (`id`, `video_id`, `status`, `total_calories`, `total_carbs`, `total_protein`, `total_fat`, `foods_json`) VALUES
(1, 1, 'DONE', 523.0, 65.2, 32.4, 14.1, '{"foods":[{"foodName":"현미밥","calories":300},{"foodName":"닭가슴살구이","calories":223}]}'),
(2, 2, 'PENDING', 0, 0, 0, 0, NULL);

-- 4-7. RECOGNIZED_FOOD_ITEM (AI가 잘라낸 디테일 푸드 아이템 세부 내역)
INSERT INTO `RECOGNIZED_FOOD_ITEM` (`id`, `nutrition_analysis_id`, `food_name`, `calories`, `carbs`, `protein`, `fat`, `quantity`) VALUES
(1, 1, '현미밥', 300, 65.2, 6.4, 1.2, 1),
(2, 1, '닭가슴살구이', 223, 0, 26, 12.9, 1);

-- 5. 최종 세션 환경 무결성 원상 복구
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;


```