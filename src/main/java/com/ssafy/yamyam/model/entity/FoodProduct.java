package com.ssafy.yamyam.model.entity;

import jakarta.persistence.Entity;
//상품
@Entity
public class FoodProduct extends Food{
    private String url; // 구매링크
}
