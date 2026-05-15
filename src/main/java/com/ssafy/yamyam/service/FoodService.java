package com.ssafy.yamyam.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yamyam.model.dto.FoodResponseDto;
import com.ssafy.yamyam.model.entity.Food;
import com.ssafy.yamyam.repository.CategoryRepository;
import com.ssafy.yamyam.repository.FoodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public FoodResponseDto getFood(Long foodId) {
        return foodRepository.findById(foodId)
                .map(FoodResponseDto::from)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음식입니다."));
    }

    @Transactional(readOnly = true)
    public List<FoodResponseDto> searchFood(String keyword, String typeFilter) {
        if (keyword == null || keyword.isBlank()) return Collections.emptyList();
        List<Food> foods;
        if ("DISH".equals(typeFilter)) {
            foods = foodRepository.findByFoodNameContainingAndFoodType(keyword, Food.FoodType.DISH);
        } else if ("PRODUCT".equals(typeFilter)) {
            foods = foodRepository.findByFoodNameContainingAndFoodType(keyword, Food.FoodType.PRODUCT);
        } else {
            foods = foodRepository.findByFoodNameContaining(keyword);
        }
        return foods.stream().map(FoodResponseDto::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getMainCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> c.getMainCategory())
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getSubCategories(String mainCategory) {
        return categoryRepository.findByMainCategory(mainCategory).stream()
                .map(c -> c.getSubCategory())
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FoodResponseDto> getFoodsByCategory(String mainCategory, String subCategory) {
        List<Food> foods;
        if (subCategory != null && !subCategory.isBlank()) {
            foods = foodRepository.findByCategorySubCategory(subCategory);
        } else {
            foods = foodRepository.findByCategoryMainCategory(mainCategory);
        }
        return foods.stream().map(FoodResponseDto::from).collect(Collectors.toList());
    }
}
