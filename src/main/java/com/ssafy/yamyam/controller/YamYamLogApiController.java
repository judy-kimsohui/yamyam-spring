package com.ssafy.yamyam.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ssafy.yamyam.model.dto.YamYamLogRequestDto;
import com.ssafy.yamyam.service.YamYamLogService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/yamyam")
@RequiredArgsConstructor
public class YamYamLogApiController extends BaseController {

    private final YamYamLogService yamYamLogService;

    // ── 기록 추가 ──────────────────────────────────────────────────
    @PostMapping("/add")
    public String add(YamYamLogRequestDto dto, HttpSession session) {
        Long memberId = getMemberId(session);
        yamYamLogService.save(memberId, dto);
        return "redirect:/yamyam/list?date=" + dto.getMealDate();
    }

    // ── 기록 수정 ──────────────────────────────────────────────────
    @PostMapping("/update")
    public String update(@RequestParam Long logId,
                         @RequestParam double servingSize) {
        yamYamLogService.update(logId, servingSize);
        return "redirect:/yamyam/detail?logId=" + logId;
    }

    // ── 기록 삭제 ──────────────────────────────────────────────────
    @PostMapping("/delete")
    public String delete(@RequestParam Long logId,
                         @RequestParam(required = false) String date) {
        yamYamLogService.delete(logId);
        String redirectDate = date != null ? date : LocalDate.now().toString();
        return "redirect:/yamyam/list?date=" + redirectDate;
    }
}
