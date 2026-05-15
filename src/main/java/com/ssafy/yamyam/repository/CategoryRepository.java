package com.ssafy.yamyam.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.yamyam.model.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 대분류 목록 조회
    List<Category> findByMainCategory(String mainCategory);

    // 대분류로 소분류 목록 조회
    List<Category> findDistinctByMainCategory(String mainCategory);
}	