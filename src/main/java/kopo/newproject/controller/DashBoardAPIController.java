package kopo.newproject.controller;

import kopo.newproject.service.IBudgetService;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
            // 1. 카테고리별 사용 금액 (pie chart)
            Map<String, Integer> categoryUsage = spendingService.getTotalAmountGroupedByCategory(userId);

            // 2. 월별 예산 vs 사용 금액 (bar chart)
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            Map<String, Integer> monthlyBudgetMap = budgetService.getTotalBudgetByMonth(userId);     // "2025-03" → 400000
            Map<String, Integer> monthlyUsedMap = spendingService.getTotalSpendingByMonth(userId);   // "2025-03" → 378000

            Set<String> allMonths = new HashSet<>();
            allMonths.addAll(monthlyBudgetMap.keySet());
            allMonths.addAll(monthlyUsedMap.keySet());

            List<String> sortedMonths = allMonths.stream()
                    .sorted() // 오름차순 정렬
                    .collect(Collectors.toList());

            for (String month : sortedMonths) {
                Map<String, Object> row = new HashMap<>();
                row.put("month", month);
                row.put("budget", monthlyBudgetMap.getOrDefault(month, 0));
                row.put("used", monthlyUsedMap.getOrDefault(month, 0));
                monthlyData.add(row);
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
