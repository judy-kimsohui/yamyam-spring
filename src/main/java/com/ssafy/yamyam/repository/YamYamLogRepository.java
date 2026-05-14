package com.ssafy.yamyam.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssafy.yamyam.model.entity.YamYamLog;

public interface YamYamLogRepository extends JpaRepository<YamYamLog, Long> {
	// 날짜 범위로 조회 (일주일치)
    List<YamYamLog> findByUserIdAndMealDateBetween(Long userId, LocalDate start, LocalDate end);
    
    // 하루치 조회
    List<YamYamLog> findByUserIdAndMealDate(Long userId, LocalDate mealDate);
}
