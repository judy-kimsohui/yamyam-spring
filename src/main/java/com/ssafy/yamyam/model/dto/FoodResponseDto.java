package com.ssafy.yamyam.model.dto;

import com.ssafy.yamyam.model.entity.Food;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoodResponseDto {
    private Long foodId;
    private Food.FoodType foodType;
    private String foodName;
    private String mainCategory;
    private String subCategory;
    private String referenceAmount;
    private double energy;
    private double protein;
    private double fat;
    private double carbs;
    private double sugar;
    private double sodium;
    private String foodWeight;
    private String url;

    public static FoodResponseDto from(Food food) {
        String mainCat = food.getCategory() != null ? food.getCategory().getMainCategory() : null;
        String subCat  = food.getCategory() != null ? food.getCategory().getSubCategory()  : null;
        return FoodResponseDto.builder()
                .foodId(food.getFoodId())
                .foodType(food.getFoodType())
                .foodName(food.getFoodName())
                .mainCategory(mainCat)
                .subCategory(subCat)
                .referenceAmount(food.getReferenceAmount())
                .energy(food.getEnergy())
                .protein(food.getProtein())
                .fat(food.getFat())
                .carbs(food.getCarbs())
                .sugar(food.getSugar())
                .sodium(food.getSodium())
                .foodWeight(food.getFoodWeight())
                .url(food.getUrl())
                .build();
    }
}
