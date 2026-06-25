# 🍱 YamYam

> **개인 맞춤형 식단 기록 웹 애플리케이션**  
> 음식을 검색하고, 섭취량을 기록하고, 영양소 섭취 현황을 한눈에 확인하세요.

---

## ✨ 주요 기능

| 기능 | 설명 |
|---|---|
| **회원가입 / 로그인** | 이메일 기반 회원 인증, PBKDF2 Salt 해싱 적용 |
| **프로필 이미지 업로드** | JPG/PNG/GIF 파일 서버 저장 및 세션 반영 |
| **음식 검색** | 67,000여 건의 식품 DB에서 이름 / 카테고리로 검색 |
| **식단 기록** | 아침·점심·저녁·간식 4가지 식사 유형으로 섭취 기록 |
| **영양소 자동 계산** | 섭취량(serving size) 입력 시 칼로리·단백질·지방·탄수화물 자동 산출 |
| **일간 / 주간 조회** | 날짜별 식단 이력 및 주간 영양소 통계 조회 |
| **식단 삭제** | 잘못 기록된 식단 항목 삭제 |

---

## 🛠 기술 스택

| 구분 | 기술 |
|---|---|
| **언어** | Java 21 |
| **프레임워크** | Spring Boot 4.0, Spring Data JPA |
| **뷰** | JSP / JSTL (Apache Tomcat Embedded) |
| **데이터베이스** | MySQL 8 |
| **빌드** | Maven (mvnw) |
| **인프라** | Docker Compose |
| **보안** | PBKDF2 + Salt 비밀번호 해싱 |
| **기타** | Lombok, Jakarta Servlet |

---

## 📁 프로젝트 구조

```
yamyam/
├── src/main/java/com/ssafy/yamyam/
│   ├── Application.java              # 진입점
│   ├── config/
│   │   └── WebConfig.java            # CORS, 정적 리소스 설정
│   ├── controller/
│   │   ├── UserController.java       # 회원 인증, 프로필
│   │   ├── YamYamLogController.java  # 식단 기록 (View)
│   │   └── YamYamLogApiController.java # 식단 기록 (REST)
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java             # 회원 엔티티
│   │   │   ├── Food.java             # 식품 엔티티 (DISH/PRODUCT)
│   │   │   ├── Category.java         # 식품 카테고리
│   │   │   └── YamYamLog.java        # 식단 기록 엔티티
│   │   └── dto/
│   │       ├── UserDto.java
│   │       ├── FoodResponseDto.java
│   │       ├── YamYamLogRequestDto.java
│   │       └── YamYamLogResponseDto.java
│   ├── repository/                   # Spring Data JPA 레포지토리
│   ├── service/
│   │   ├── UserService.java          # 인증, 이미지 업로드
│   │   └── YamYamLogService.java     # 식단 CRUD, 음식 검색
│   └── util/
│       ├── PasswordHashingUtil.java  # PBKDF2 Salt 해싱
│       └── DBUtils.java
├── src/main/resources/
│   ├── application.properties
│   └── scripts/
│       ├── schema.sql                # 테이블 DDL
│       ├── data.sql                  # 식품 시드 데이터
│       └── test.sql
├── src/main/webapp/WEB-INF/views/    # JSP 뷰
│   ├── user/        (login, register, main, mypage, ...)
│   ├── food/        (food_search, food_select)
│   └── yamyamlog/   (yamyamlog_list, yamyamlog_detail)
├── docker-compose.yml
├── pom.xml
└── food.csv                          # 원본 식품 데이터 (~67,000건)
```

---

## 🗄 데이터베이스 설계

```
category          food
─────────         ────────────────────
category_id  ◄──  category_id (FK)
main_category     food_id
sub_category      food_name
                  food_type (DISH/PRODUCT)
                  energy, protein, fat, carbs, sugar, sodium
                  reference_amount, food_weight

member (User)     yamyam_log
─────────────     ─────────────────────
user_id      ◄──  user_id (FK)
email             log_id
password          food_id (FK) ──► food
nickname          meal_date
age               meal_type (BREAKFAST/LUNCH/DINNER/SNACK)
height, weight    serving_size
profile_image     actual_energy, actual_protein, actual_fat, actual_carbs
salt              created_at
```

---

## 🚀 빠른 시작

### 사전 요구사항

- Java 21
- Docker & Docker Compose
- Maven 3.9+

### 1. 저장소 클론

```bash
git clone https://github.com/ssafy/yamyam.git
cd yamyam
```

### 2. MySQL 컨테이너 실행

```bash
docker-compose up -d
```

> MySQL 8이 `localhost:3307`에 실행됩니다. (데이터베이스: `yamyamdb`, 패스워드: `ssafy`)

### 3. 스키마 및 시드 데이터 적용

```bash
mysql -h 127.0.0.1 -P 3307 -u root -pssafy yamyamdb < src/main/resources/scripts/schema.sql
mysql -h 127.0.0.1 -P 3307 -u root -pssafy yamyamdb < src/main/resources/scripts/data.sql
```

### 4. 애플리케이션 실행

```bash
./mvnw spring-boot:run
```

브라우저에서 `http://localhost:8080/user/login` 으로 접속하세요.

---

## ⚙️ 환경 설정

`src/main/resources/application.properties`의 주요 설정값:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/yamyamdb
spring.datasource.username=root
spring.datasource.password=ssafy

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

file.upload-dir=/path/to/upload/dir    # 프로필 이미지 저장 경로 수정 필요
```

---

## 🔒 보안

- 비밀번호는 평문으로 저장되지 않습니다. 회원가입 시 **랜덤 Salt 생성 → PBKDF2 해싱** 후 저장합니다.
- 로그인 성공 시 사용자 정보는 **HTTP Session**에 저장하며, 민감 정보(password, salt)는 세션에 포함하지 않습니다.
- 프로필 이미지 업로드는 허용 확장자(`.jpg` `.jpeg` `.png` `.gif`)만 처리합니다.

---

## 📌 향후 개선 사항

- [ ] Spring Security 적용 (JWT 또는 Session 강화)
- [ ] 주간/월간 영양소 통계 차트 시각화
- [ ] 음식 즐겨찾기 기능
- [ ] 모바일 반응형 UI
- [ ] CI/CD 파이프라인 구축 (GitHub Actions)
- [ ] 단위 테스트 커버리지 80% 이상 달성

---

## 👥 팀

| 역할 | 이름 |
|---|---|
| 기획 / 백엔드 | SSAFY |
| 프론트엔드 | SSAFY |

---

## 📄 라이선스

이 프로젝트는 교육 목적으로 개발되었습니다.
