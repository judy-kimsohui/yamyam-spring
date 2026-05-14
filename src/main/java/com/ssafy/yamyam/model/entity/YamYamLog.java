package com.ssafy.yamyam.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "yamyam_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YamYamLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "serving_size", nullable = false)
    private double servingSize;

    @Column(name = "actual_energy")
    private double actualEnergy;

    @Column(name = "actual_protein")
    private double actualProtein;

    @Column(name = "actual_fat")
    private double actualFat;

    @Column(name = "actual_carbs")
    private double actualCarbs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.food != null) {
            this.actualEnergy  = this.food.getEnergy()  * servingSize;
            this.actualProtein = this.food.getProtein() * servingSize;
            this.actualFat     = this.food.getFat()     * servingSize;
            this.actualCarbs   = this.food.getCarbs()   * servingSize;
        }
    }

    public enum MealType {
        BREAKFAST,
        LUNCH,
        DINNER,
        SNACK
    }
}