-- =====================================================
-- 영양 분석 결과 테이블
-- =====================================================
CREATE TABLE nutrition_analysis (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    video_id       BIGINT       NOT NULL,                       -- video 테이블 FK
    status         VARCHAR(10)  NOT NULL DEFAULT 'PENDING',     -- PENDING / DONE / FAILED
    total_calories DOUBLE,
    total_carbs    DOUBLE,
    total_protein  DOUBLE,
    total_fat      DOUBLE,
    foods_json     TEXT,                                        -- Claude 원본 응답 보관
    error_message  VARCHAR(500),
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_video_id (video_id),                         -- 영상 1개당 분석 1개
    CONSTRAINT fk_nutrition_video
        FOREIGN KEY (video_id) REFERENCES video (id)
        ON DELETE CASCADE
);

-- =====================================================
-- 인식된 개별 음식 항목 테이블
-- =====================================================
CREATE TABLE recognized_food_item (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    nutrition_analysis_id BIGINT       NOT NULL,
    food_name             VARCHAR(100) NOT NULL,
    calories              DOUBLE,
    carbs                 DOUBLE,
    protein               DOUBLE,
    fat                   DOUBLE,

    PRIMARY KEY (id),
    CONSTRAINT fk_food_item_analysis
        FOREIGN KEY (nutrition_analysis_id) REFERENCES nutrition_analysis (id)
        ON DELETE CASCADE
);
