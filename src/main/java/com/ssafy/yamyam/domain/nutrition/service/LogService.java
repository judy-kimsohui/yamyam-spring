package com.ssafy.yamyam.domain.nutrition.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ssafy.yamyam.domain.nutrition.mapper.NutritionMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {
    private final NutritionMapper nutritionMapper;

    public Map<String, Object> getDailyLogData(Long userId, String date) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> summary = nutritionMapper.findDailySummary(userId, date);
        result.put("total", summary != null ? summary : new HashMap<>());
        result.put("logs", nutritionMapper.findDailyLogs(userId, date));

        return result;
    }

    public Map<String, Object> getTrendData(Long userId, String period) {
        LocalDate base = LocalDate.now();
        List<Bucket> buckets = buildBuckets(base, period);
        LocalDate start = buckets.get(0).dates().get(0);
        List<LocalDate> lastDates = buckets.get(buckets.size() - 1).dates();
        LocalDate end = lastDates.get(lastDates.size() - 1);

        Map<LocalDate, Map<String, Object>> rowsByDate = nutritionMapper
            .findDailyNutritionTrend(userId, start.toString(), end.toString())
            .stream()
            .collect(Collectors.toMap(
                row -> LocalDate.parse(String.valueOf(row.get("mealDate"))),
                Function.identity()
            ));

        List<Map<String, Object>> filledBuckets = new ArrayList<>();
        for (Bucket bucket : buckets) {
            Map<String, Object> totals = new HashMap<>();
            totals.put("calories", 0.0);
            totals.put("carbs", 0.0);
            totals.put("protein", 0.0);
            totals.put("fat", 0.0);
            totals.put("count", 0);

            for (LocalDate date : bucket.dates()) {
                Map<String, Object> row = rowsByDate.get(date);
                if (row == null) {
                    continue;
                }

                totals.put("calories", toDouble(totals.get("calories")) + toDouble(row.get("calories")));
                totals.put("carbs", toDouble(totals.get("carbs")) + toDouble(row.get("carbs")));
                totals.put("protein", toDouble(totals.get("protein")) + toDouble(row.get("protein")));
                totals.put("fat", toDouble(totals.get("fat")) + toDouble(row.get("fat")));
                totals.put("count", ((Number) totals.get("count")).intValue() + 1);
            }

            Map<String, Object> bucketResult = new HashMap<>();
            bucketResult.put("label", bucket.label());
            bucketResult.put("totals", totals);
            filledBuckets.add(bucketResult);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("calories", toSeries(filledBuckets, "calories"));
        result.put("carbs", toSeries(filledBuckets, "carbs"));
        result.put("protein", toSeries(filledBuckets, "protein"));
        result.put("fat", toSeries(filledBuckets, "fat"));
        result.put("summary", buildSummary(filledBuckets));
        return result;
    }

    private List<Bucket> buildBuckets(LocalDate base, String period) {
        if ("day".equals(period)) {
            List<Bucket> buckets = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = base.minusDays(i);
                buckets.add(new Bucket(i == 0 ? "today" : date.getMonthValue() + "/" + date.getDayOfMonth(), List.of(date)));
            }
            return buckets;
        }

        if ("month".equals(period)) {
            List<Bucket> buckets = new ArrayList<>();
            LocalDate cursor = base.withDayOfMonth(1);
            int week = 1;
            while (!cursor.isAfter(base)) {
                LocalDate start = cursor;
                LocalDate end = start.plusDays(6).isAfter(base) ? base : start.plusDays(6);
                buckets.add(new Bucket(week + "\uC8FC\uCC28", datesBetween(start, end)));
                cursor = end.plusDays(1);
                week++;
            }
            return buckets;
        }

        List<Bucket> buckets = new ArrayList<>();
        LocalDate startOfWeek = base.minusDays(base.getDayOfWeek().getValue() - 1L);
        LocalDate cursor = startOfWeek;
        while (!cursor.isAfter(base)) {
            buckets.add(new Bucket(dayLabel(cursor), List.of(cursor)));
            cursor = cursor.plusDays(1);
        }
        return buckets;
    }

    private String dayLabel(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "\uC6D4";
            case TUESDAY -> "\uD654";
            case WEDNESDAY -> "\uC218";
            case THURSDAY -> "\uBAA9";
            case FRIDAY -> "\uAE08";
            case SATURDAY -> "\uD1A0";
            case SUNDAY -> "\uC77C";
        };
    }

    private List<LocalDate> datesBetween(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            dates.add(cursor);
            cursor = cursor.plusDays(1);
        }
        return dates;
    }

    private List<Map<String, Object>> toSeries(List<Map<String, Object>> buckets, String key) {
        List<Map<String, Object>> series = new ArrayList<>();
        for (Map<String, Object> bucket : buckets) {
            Map<String, Object> totals = (Map<String, Object>) bucket.get("totals");
            Map<String, Object> point = new HashMap<>();
            point.put("label", bucket.get("label"));
            point.put("value", Math.round(toDouble(totals.get(key))));
            point.put("isForecast", false);
            point.put("hasRecord", ((Number) totals.get("count")).intValue() > 0);
            series.add(point);
        }
        return series;
    }

    private Map<String, Object> buildSummary(List<Map<String, Object>> buckets) {
        double calories = 0.0;
        int recordedDays = 0;
        for (Map<String, Object> bucket : buckets) {
            Map<String, Object> totals = (Map<String, Object>) bucket.get("totals");
            int count = ((Number) totals.get("count")).intValue();
            if (count > 0) {
                calories += toDouble(totals.get("calories"));
                recordedDays += count;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("avgCalories", recordedDays > 0 ? Math.round(calories / recordedDays) : 0);
        summary.put("recordedDays", recordedDays);
        return summary;
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private record Bucket(String label, List<LocalDate> dates) {}
}
