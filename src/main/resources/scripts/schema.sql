# DROP TABLE IF EXISTS yamyam_log;
# DROP TABLE IF EXISTS food;
# DROP TABLE IF EXISTS member;
# DROP TABLE IF EXISTS category;

CREATE TABLE category (
                          category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          main_category VARCHAR(100),
                          sub_category VARCHAR(100)
);

CREATE TABLE food (
                      food_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      food_code VARCHAR(100),

                      food_type VARCHAR(20) NOT NULL COMMENT 'DISH/PRODUCT',

                      food_name VARCHAR(255) NOT NULL,
                      category_id BIGINT,

                      reference_amount VARCHAR(100),
                      energy DOUBLE NOT NULL DEFAULT 0,
                      protein DOUBLE NOT NULL DEFAULT 0,
                      fat DOUBLE NOT NULL DEFAULT 0,
                      carbs DOUBLE NOT NULL DEFAULT 0,
                      sugar DOUBLE NOT NULL DEFAULT 0,
                      sodium DOUBLE NOT NULL DEFAULT 0,
                      food_weight VARCHAR(100),

                      url VARCHAR(1000),

                      CONSTRAINT fk_food_category
                          FOREIGN KEY (category_id)
                              REFERENCES category(category_id)
);

CREATE TABLE member (
                        member_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                        email VARCHAR(255) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        age INT NOT NULL,

                        gender VARCHAR(20) NOT NULL COMMENT 'MALE/FEMALE/OTHER',

                        height FLOAT NOT NULL,
                        weight FLOAT NOT NULL,

                        health_goal VARCHAR(30) NOT NULL COMMENT 'WEIGHT_LOSS/MUSCLE_GAIN/MAINTENANCE'
);

CREATE TABLE yamyam_log (
                            log_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                            member_id BIGINT NOT NULL,
                            meal_date DATE NOT NULL,
                            meal_type VARCHAR(20) NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SNACK',

                            food_id BIGINT NOT NULL,

                            serving_size DOUBLE NOT NULL,

                            actual_energy DOUBLE,
                            actual_protein DOUBLE,
                            actual_fat DOUBLE,
                            actual_carbs DOUBLE,

                            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_yamyam_log_member
                                FOREIGN KEY (member_id)
                                    REFERENCES member(member_id),

                            CONSTRAINT fk_yamyam_log_food
                                FOREIGN KEY (food_id)
                                    REFERENCES food(food_id)
);