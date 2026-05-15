package com.ssafy.yamyam.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private float height;   // cm

    @Column(nullable = false)
    private float weight;   // kg

    @Enumerated(EnumType.STRING)
    @Column(name = "health_goal", nullable = false)
    private HealthGoal healthGoal;

    // ── Enum ────────────────────────────────

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum HealthGoal {
        WEIGHT_LOSS,    // 체중 감량
        MUSCLE_GAIN,    // 근육 증가
        MAINTENANCE     // 유지
    }
}