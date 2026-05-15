package com.ssafy.yamyam.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yamyam.model.dto.YamYamLogRequestDto;
import com.ssafy.yamyam.model.dto.YamYamLogResponseDto;
import com.ssafy.yamyam.model.entity.Food;
import com.ssafy.yamyam.model.entity.Member;
import com.ssafy.yamyam.model.entity.YamYamLog;
import com.ssafy.yamyam.repository.FoodRepository;
import com.ssafy.yamyam.repository.MemberRepository;
import com.ssafy.yamyam.repository.YamYamLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class YamYamLogService {

    private final YamYamLogRepository yamYamLogRepository;
    private final FoodRepository foodRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public YamYamLogResponseDto save(Long memberId, YamYamLogRequestDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Food food = foodRepository.findById(dto.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 음식입니다."));

        YamYamLog log = YamYamLog.builder()
                .member(member)
                .food(food)
                .mealDate(dto.getMealDate())
                .mealType(dto.getMealType())
                .servingSize(dto.getServingSize())
                .build();

        return YamYamLogResponseDto.from(yamYamLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<YamYamLogResponseDto> getDaily(Long memberId, LocalDate date) {
        return yamYamLogRepository.findByMember_MemberIdAndMealDate(memberId, date)
                .stream()
                .map(YamYamLogResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<YamYamLogResponseDto> getWeekly(Long memberId, LocalDate start) {
        return yamYamLogRepository.findByMember_MemberIdAndMealDateBetween(
                        memberId, start, start.plusDays(6))
                .stream()
                .map(YamYamLogResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public YamYamLogResponseDto getLog(Long logId) {
        return yamYamLogRepository.findById(logId)
                .map(YamYamLogResponseDto::from)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다."));
    }

    @Transactional
    public void update(Long logId, double servingSize) {
        YamYamLog log = yamYamLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 기록입니다."));
        double ratio = servingSize / 100.0;
        log.setServingSize(servingSize);
        log.setActualEnergy(log.getFood().getEnergy()   * ratio);
        log.setActualProtein(log.getFood().getProtein() * ratio);
        log.setActualFat(log.getFood().getFat()         * ratio);
        log.setActualCarbs(log.getFood().getCarbs()     * ratio);
    }

    @Transactional
    public void delete(Long logId) {
        yamYamLogRepository.deleteById(logId);
    }
}
