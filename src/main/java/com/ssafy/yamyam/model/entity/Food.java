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

    private String foodName;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    
    private String referenceAmount;
    private double energy;
    private double protein;
    private double fat;
    private double carbs;
    private double sugar;
    private double sodium;
    private String foodWeight;
}