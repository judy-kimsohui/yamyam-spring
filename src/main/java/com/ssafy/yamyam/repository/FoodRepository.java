package com.ssafy.yamyam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.yamyam.model.entity.Category;
import com.ssafy.yamyam.model.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByFoodNameContaining(String keyword);
    List<Food> findByFoodNameContainingAndFoodType(String keyword, Food.FoodType foodType);
    
    
    // 카테고리로 음식 목록 조회
    List<Food> findByCategory(Category category);

    // 대분류로 음식 목록 조회
    List<Food> findByCategoryMainCategory(String mainCategory);

    // 소분류로 음식 목록 조회
    List<Food> findByCategorySubCategory(String subCategory);

}