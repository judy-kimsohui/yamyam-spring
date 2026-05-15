package com.ssafy.yamyam.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long foodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_type")
    private FoodType foodType;

    private String foodCode;          // 식품코드

    private String foodName;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


    private String referenceAmount; // 영양성분함량기준량 (100g)
    private double energy;          // 칼로리(kcal)
    private double protein;         // 단백질
    private double fat;             // 지방
    private double carbs;           // 탄수화물
    private double sugar;           // 당류(g)
    private double sodium;          // 나트륨(mg)
    private String foodWeight;      // 식품중량(g)


    public enum FoodType {
        DISH,
        PRODUCT
    }

}