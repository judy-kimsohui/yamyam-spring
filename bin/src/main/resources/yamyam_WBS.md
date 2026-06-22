# YamYam 프로젝트 WBS (Work Breakdown Structure)

> **프로젝트명:** YamYam — 개인 맞춤형 식단 기록 웹 애플리케이션  
> **기술 스택:** Spring Boot 4.0 · Spring Data JPA · MySQL 8 · JSP/JSTL · Docker  
> **기간:** 2주 (14일 스프린트)

---

## 1. 프로젝트 관리 (Project Management)

| ID | 작업 항목 | 산출물 | 담당 | 일정 |
|---|---|---|---|---|
| 1.1 | 요구사항 분석 | 기능 목록, ERD 초안 | 전체 | D1 |
| 1.2 | 개발 환경 표준화 | .gitignore, .editorconfig | 전체 | D1 |
| 1.3 | 브랜치 전략 수립 | main/dev 브랜치 구조 | 전체 | D1 |
| 1.4 | WBS / 일정 계획 | 간트 차트 | 전체 | D1 |

---

## 2. 인프라 및 환경 설정 (Infrastructure & Setup)

| ID | 작업 항목 | 산출물 | 담당 | 일정 |
|---|---|---|---|---|
| 2.1 | Spring Boot 프로젝트 초기화 | pom.xml, Application.java | 개발 | D1 |
| 2.2 | MySQL 8 Docker Compose 구성 | docker-compose.yml | 개발 | D1 |
| 2.3 | application.properties 설정 | DB연결, 파일 업로드 경로 | 개발 | D1 |
| 2.4 | CORS / WebConfig 설정 | WebConfig.java | 개발 | D2 |
| 2.5 | 공통 예외 처리 구조 설계 | GeneralException.java | 개발 | D2 |

---

## 3. 데이터베이스 설계 (Database Design)

| ID | 작업 항목 | 산출물 | 담당 | 일정 |
|---|---|---|---|---|
| 3.1 | ERD 설계 (4개 테이블) | schema.sql | 개발 | D2 |
| 3.2 | `category` 테이블 생성 | DDL | 개발 | D2 |
| 3.3 | `food` 테이블 생성 | DDL | 개발 | D2 |
| 3.4 | `member`(User) 테이블 생성 | DDL | 개발 | D2 |
| 3.5 | `yamyam_log` 테이블 생성 | DDL | 개발 | D2 |
| 3.6 | food.csv 데이터 파싱·적재 | data.sql (~67,000건) | 개발 | D5–D7 |
| 3.7 | 테스트 시드 데이터 작성 | test.sql | 개발 | D7 |

---

## 4. 백엔드 개발 (Backend Development)

### 4.1 도메인 모델 (Entity)

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 4.1.1 | `User` 엔티티 구현 | User.java | D3 |
| 4.1.2 | `Food` 엔티티 구현 (FoodType: DISH/PRODUCT) | Food.java | D3 |
| 4.1.3 | `Category` 엔티티 구현 | Category.java | D3 |
| 4.1.4 | `YamYamLog` 엔티티 구현 (MealType Enum, @PrePersist) | YamYamLog.java | D3 |
| 4.1.5 | DTO 클래스 구현 (UserDto, FoodResponseDto, YamYamLogRequestDto, YamYamLogResponseDto) | dto/*.java | D4 |

### 4.2 Repository

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 4.2.1 | `UserRepository` (findByEmail, findSaltByUserId, insertUserSalt) | UserRepository.java | D3 |
| 4.2.2 | `FoodRepository` (카테고리 필터, 키워드 검색) | FoodRepository.java | D4 |
| 4.2.3 | `CategoryRepository` (대분류/소분류 조회) | CategoryRepository.java | D4 |
| 4.2.4 | `YamYamLogRepository` (날짜별/기간별 조회) | YamYamLogRepository.java | D4 |

### 4.3 Service

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 4.3.1 | `UserService.login()` — Salt 기반 비밀번호 검증 | UserService.java | D4 |
| 4.3.2 | `UserService.signup()` — 이메일 중복 체크, Salt 생성·저장 | UserService.java | D4 |
| 4.3.3 | `UserService.updateProfileImage()` — 파일 확장자 검증, 저장 | UserService.java | D6 |
| 4.3.4 | `YamYamLogService.save()` — 식단 기록 저장 + 영양소 계산 | YamYamLogService.java | D8 |
| 4.3.5 | `YamYamLogService.getDaily()` — 일간 조회 | YamYamLogService.java | D9 |
| 4.3.6 | `YamYamLogService.getWeekly()` — 주간 조회 | YamYamLogService.java | D9 |
| 4.3.7 | `YamYamLogService.delete()` — 식단 기록 삭제 | YamYamLogService.java | D10 |
| 4.3.8 | `YamYamLogService.getMainCategories()` / `getSubCategories()` | YamYamLogService.java | D8 |
| 4.3.9 | `YamYamLogService.searchFood()` — 음식명 검색 | YamYamLogService.java | D9 |

### 4.4 Controller

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 4.4.1 | `UserController` — 회원가입·로그인·프로필 업로드 | UserController.java | D5 |
| 4.4.2 | `YamYamLogController` — 식단 기록 View 연동 | YamYamLogController.java | D9 |
| 4.4.3 | `YamYamLogApiController` — REST API 엔드포인트 | YamYamLogApiController.java | D10 |

### 4.5 Utility

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 4.5.1 | `PasswordHashingUtil` — PBKDF2 Salt 생성·해싱 | PasswordHashingUtil.java | D3 |
| 4.5.2 | `DBUtils` — DB 유틸리티 | DBUtils.java | D3 |

---

## 5. 프론트엔드 개발 (Frontend / View)

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 5.1 | 공통 레이아웃 (header / footer) | common/*.jsp | D5 |
| 5.2 | 로그인 페이지 | user/login.jsp | D5 |
| 5.3 | 회원가입 페이지 (나이, 키, 몸무게 입력) | user/register.jsp | D5 |
| 5.4 | 메인 페이지 (세션 기반 로그인 확인) | user/main.jsp | D8 |
| 5.5 | 마이페이지 | user/mypage.jsp | D9 |
| 5.6 | 프로필 이미지 업로드 페이지 | user/updateProfileImage.jsp | D8 |
| 5.7 | 음식 검색 페이지 | food/food_search.jsp | D9 |
| 5.8 | 음식 상세 선택 페이지 | food/food_select.jsp | D9 |
| 5.9 | 식단 로그 목록 페이지 | yamyamlog/yamyamlog_list.jsp | D10 |
| 5.10 | 식단 로그 상세 페이지 | yamyamlog/yamyamlog_detail.jsp | D10 |
| 5.11 | 공통 CSS 스타일 | css/style.css | D5 |

---

## 6. 테스트 (Testing)

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 6.1 | 단위 테스트 (UserService, YamYamLogService) | ApplicationTests.java | D11 |
| 6.2 | 회원가입/로그인 통합 테스트 | 테스트 결과 보고서 | D12 |
| 6.3 | 식단 기록 CRUD 통합 테스트 | 테스트 결과 보고서 | D12 |
| 6.4 | 프로필 이미지 업로드 테스트 | 테스트 결과 보고서 | D12 |
| 6.5 | 크로스브라우저 JSP 렌더링 검증 | 체크리스트 | D13 |

---

## 7. 배포 및 마무리 (Deployment & Wrap-up)

| ID | 작업 항목 | 산출물 | 일정 |
|---|---|---|---|
| 7.1 | Docker Compose 최종 점검 | docker-compose.yml | D13 |
| 7.2 | 환경 변수 / 시크릿 분리 | .env 파일 | D13 |
| 7.3 | README.md 최종 작성 | README.md | D13 |
| 7.4 | 코드 리뷰 및 리팩토링 | PR 리뷰 내역 | D14 |
| 7.5 | 최종 산출물 정리 | 릴리즈 태그 v1.0.0 | D14 |

---

## WBS 요약

```
YamYam 프로젝트
├── 1. 프로젝트 관리          D1
├── 2. 인프라/환경 설정       D1–D2
├── 3. 데이터베이스 설계      D2–D7
├── 4. 백엔드 개발
│   ├── 4.1 Entity           D3
│   ├── 4.2 Repository       D3–D4
│   ├── 4.3 Service          D4–D10
│   ├── 4.4 Controller       D5–D10
│   └── 4.5 Utility          D3
├── 5. 프론트엔드 (JSP)       D5–D10
├── 6. 테스트                 D11–D13
└── 7. 배포/마무리            D13–D14
```
