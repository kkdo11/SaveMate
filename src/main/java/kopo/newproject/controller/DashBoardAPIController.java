package kopo.newproject.controller;

import kopo.newproject.service.IBudgetService;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboardAPI")
@RequiredArgsConstructor
public class DashBoardAPIController {

    private final ISpendingService spendingService;
    private final IBudgetService budgetService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    // 📊 차트용 데이터 API
    @GetMapping("/usage-summary")
    public ResponseEntity<?> getDashboardSummary() {
        String userId = getCurrentUserId();

        try {
            // 1. 카테고리별 사용 금액 (pie chart) - 현재 월 기준
            YearMonth currentMonth = YearMonth.now(ZoneId.of("Asia/Seoul"));
            Map<String, Integer> categoryUsage = spendingService.getTotalAmountGroupedByCategory(userId, currentMonth);

            // 2. 월별 예산 vs 사용 금액 (bar chart) - 최근 6개월
            YearMonth endMonth = YearMonth.now(ZoneId.of("Asia/Seoul"));
            YearMonth startMonth = endMonth.minusMonths(5);

            Map<String, Integer> monthlyBudgetMap = budgetService.getTotalBudgetByMonth(userId, startMonth, endMonth);
            Map<String, Integer> monthlyUsedMap = spendingService.getTotalSpendingByMonth(userId, startMonth, endMonth);

            List<Map<String, Object>> monthlyData = new ArrayList<>();
            YearMonth currentMonthIter = startMonth;
            while (!currentMonthIter.isAfter(endMonth)) {
                String monthKey = currentMonthIter.toString();
                Map<String, Object> row = new HashMap<>();
                row.put("month", monthKey);
                row.put("budget", monthlyBudgetMap.getOrDefault(monthKey, 0));
                row.put("used", monthlyUsedMap.getOrDefault(monthKey, 0));
                monthlyData.add(row);
                currentMonthIter = currentMonthIter.plusMonths(1);
            }

            // 🔥 리턴 형식
            Map<String, Object> response = new HashMap<>();
            response.put("categoryUsage", categoryUsage);
            response.put("monthlyBudget", monthlyData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("대시보드 데이터 조회 실패");
        }
    }
}
