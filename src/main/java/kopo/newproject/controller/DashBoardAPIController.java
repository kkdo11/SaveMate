package kopo.newproject.controller;

import kopo.newproject.service.IBudgetService;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 대시보드에 필요한 데이터를 제공하는 API 컨트롤러.
 * <p>
 * {@code @RestController} - 이 컨트롤러의 모든 메소드는 JSON 형태의 데이터를 반환합니다.
 * {@code @RequestMapping("/api/dashboard")} - 이 컨트롤러의 모든 API는 '/api/dashboard' 경로 하위에 매핑됩니다.
 * {@code @RequiredArgsConstructor} - final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard") // RESTful API 경로 규칙에 따라 수정
@RequiredArgsConstructor
public class DashBoardAPIController {

    private final ISpendingService spendingService;
    private final IBudgetService budgetService;

    /**
     * Spring Security 컨텍스트에서 현재 인증된 사용자의 ID를 안전하게 가져오는 헬퍼 메소드.
     * @return 현재 로그인된 사용자의 ID
     */
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * 대시보드의 차트를 그리는데 필요한 요약 데이터를 조회하는 API.
     * <p>
     * 반환되는 데이터 구조:
     * <pre>
     * {
     *   "categoryUsage": { "식비": 150000, "교통": 50000, ... },
     *   "monthlyBudget": [
     *     { "month": "2025-01", "budget": 500000, "used": 450000 },
     *     { "month": "2025-02", "budget": 500000, "used": 480000 },
     *     ...
     *   ]
     * }
     * </pre>
     * @return 대시보드 요약 데이터를 포함하는 ResponseEntity
     */
    @GetMapping("/usage-summary")
    public ResponseEntity<?> getDashboardSummary() {
        log.info("▶▶▶ [API Start] getDashboardSummary");
        try {
            String userId = getCurrentUserId();
            log.info("대시보드 데이터 조회 시작 | userId: {}", userId);

            // 1. 카테고리별 총 사용 금액 조회 (Pie Chart용 데이터)
            Map<String, Integer> categoryUsage = spendingService.getTotalAmountGroupedByCategory(userId);
            log.info("카테고리별 사용 금액 조회 완료 | 조회된 카테고리 수: {}", categoryUsage.size());

            // 2. 월별 예산 vs 사용 금액 조회 (Bar Chart용 데이터)
            // 각 서비스에서 월별 예산과 월별 지출 데이터를 각각 가져옴
            Map<String, Integer> monthlyBudgetMap = budgetService.getTotalBudgetByMonth(userId);
            Map<String, Integer> monthlyUsedMap = spendingService.getTotalSpendingByMonth(userId);
            log.info("월별 예산 및 지출 데이터 조회 완료 | 예산 월 수: {}, 지출 월 수: {}", monthlyBudgetMap.size(), monthlyUsedMap.size());

            // 예산 월과 지출 월을 모두 합쳐 전체 기간을 구함 (예: 한쪽 데이터만 있는 월도 포함하기 위함)
            Set<String> allMonths = new HashSet<>();
            allMonths.addAll(monthlyBudgetMap.keySet());
            allMonths.addAll(monthlyUsedMap.keySet());

            // 월(YYYY-MM)을 기준으로 오름차순 정렬
            List<String> sortedMonths = allMonths.stream().sorted().collect(Collectors.toList());
            log.info("차트용 월 목록 정렬 완료 | 전체 월 수: {}", sortedMonths.size());

            // 최종적으로 반환할 월별 데이터 리스트 생성
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            for (String month : sortedMonths) {
                Map<String, Object> row = new HashMap<>();
                row.put("month", month);
                row.put("budget", monthlyBudgetMap.getOrDefault(month, 0));
                row.put("used", monthlyUsedMap.getOrDefault(month, 0));
                monthlyData.add(row);
            }

            // 3. 최종 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("categoryUsage", categoryUsage);
            response.put("monthlyBudget", monthlyData);

            log.info("대시보드 데이터 구성 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("대시보드 데이터 조회 중 에러 발생", e);
            return ResponseEntity.status(500).body("대시보드 데이터 조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getDashboardSummary");
        }
    }
}
