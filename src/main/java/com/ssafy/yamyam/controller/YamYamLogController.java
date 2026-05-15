package com.ssafy.yamyam.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ssafy.yamyam.model.dto.FoodResponseDto;
import com.ssafy.yamyam.model.dto.YamYamLogResponseDto;
import com.ssafy.yamyam.model.entity.YamYamLog.MealType;
import com.ssafy.yamyam.service.FoodService;
import com.ssafy.yamyam.service.YamYamLogService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/yamyam")
@RequiredArgsConstructor
public class YamYamLogController extends BaseController {

    private final YamYamLogService yamYamLogService;
    private final FoodService foodService;

    // ── 식단 목록 ──────────────────────────────────────────────────
    @GetMapping("/list")
    public String list(@RequestParam(required = false) String date,
                       Model model, HttpSession session) {
        LocalDate logDate = parseDate(date, LocalDate.now());
        Long memberId = getMemberId(session);

        List<YamYamLogResponseDto> logs = yamYamLogService.getDaily(memberId, logDate);

        Map<String, List<YamYamLogResponseDto>> mealMap = new LinkedHashMap<>();
        for (MealType mt : MealType.values()) {
            mealMap.put(mt.name(), logs.stream()
                    .filter(l -> l.getMealType() == mt)
                    .collect(Collectors.toList()));
        }

        double totalEnergy  = logs.stream().mapToDouble(YamYamLogResponseDto::getActualEnergy).sum();
        double totalProtein = logs.stream().mapToDouble(YamYamLogResponseDto::getActualProtein).sum();
        double totalFat     = logs.stream().mapToDouble(YamYamLogResponseDto::getActualFat).sum();
        double totalCarbs   = logs.stream().mapToDouble(YamYamLogResponseDto::getActualCarbs).sum();

        model.addAttribute("logDate",      logDate.toString());
        model.addAttribute("today",        LocalDate.now().toString());
        model.addAttribute("mealMap",      mealMap);
        model.addAttribute("totalEnergy",  totalEnergy);
        model.addAttribute("totalProtein", totalProtein);
        model.addAttribute("totalFat",     totalFat);
        model.addAttribute("totalCarbs",   totalCarbs);
        model.addAttribute("goalEnergy",   2000.0);
        return "yamyamlog/yamyamlog_list";
    }

    // ── 기록 상세 ──────────────────────────────────────────────────
    @GetMapping("/detail")
    public String detail(@RequestParam Long logId, Model model) {
        YamYamLogResponseDto log = yamYamLogService.getLog(logId);
        FoodResponseDto food = foodService.getFood(log.getFoodId());
        model.addAttribute("log",  log);
        model.addAttribute("food", food);
        return "yamyamlog/yamyamlog_detail";
    }

    private LocalDate parseDate(String date, LocalDate fallback) {
        if (date == null || date.isBlank()) return fallback;
        try { return LocalDate.parse(date); }
        catch (DateTimeParseException e) { return fallback; }
    }
}
