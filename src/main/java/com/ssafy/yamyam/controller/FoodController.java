package com.ssafy.yamyam.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ssafy.yamyam.model.dto.FoodResponseDto;
import com.ssafy.yamyam.service.FoodService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/food")
@RequiredArgsConstructor
public class FoodController extends BaseController {

    private final FoodService foodService;

    // ── 음식 검색 ──────────────────────────────────────────────────
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String mealType,
                         @RequestParam(required = false) String date,
                         @RequestParam(required = false, defaultValue = "ALL") String typeFilter,
                         Model model) {
        List<FoodResponseDto> foodList = foodService.searchFood(keyword, typeFilter);
        model.addAttribute("keyword",    keyword);
        model.addAttribute("mealType",   mealType);
        model.addAttribute("date",       date != null ? date : LocalDate.now().toString());
        model.addAttribute("typeFilter", typeFilter);
        model.addAttribute("foodList",   foodList);
        model.addAttribute("total",      foodList.size());
        return "food/food_search";
    }

    // ── 섭취량 입력 ────────────────────────────────────────────────
    @GetMapping("/select")
    public String select(@RequestParam(required = false) Long foodId,
                         @RequestParam(required = false) String mealType,
                         @RequestParam(required = false) String date,
                         Model model) {
        if (foodId == null) {
            String redirect = "/food/search?date=" + (date != null ? date : LocalDate.now().toString());
            if (mealType != null) redirect += "&mealType=" + mealType;
            return "redirect:" + redirect;
        }
        FoodResponseDto food = foodService.getFood(foodId);
        model.addAttribute("food",     food);
        model.addAttribute("mealType", mealType);
        model.addAttribute("date",     date != null ? date : LocalDate.now().toString());
        return "food/food_select";
    }
}
