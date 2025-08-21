package kopo.newproject.service.impl;

import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.repository.jpa.BudgetRepository;
import kopo.newproject.repository.mongo.SpendingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisPreprocessorService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisPreprocessorService.class);

    private final SpendingRepository spendingRepo;
    private final BudgetRepository budgetRepo;



    public Map<String, Object> generateAnalysisInput(String userId, YearMonth yearMonth) {
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        // 📦 1. 소비 데이터 조회
        List<SpendingEntity> spendings = spendingRepo.findByUserIdAndDateBetween(userId, from, to);

        // 📦 2. 예산 데이터 조회 (변경된 메서드 사용)
        List<BudgetEntity> budgets = budgetRepo.findAllByUserIdAndYearAndMonth(userId, yearMonth.getYear(), yearMonth.getMonthValue());

        // 📊 3. 소비 데이터 전처리
        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        Map<String, List<String>> descriptionByCategory = new HashMap<>();
        BigDecimal totalSpending = BigDecimal.ZERO;

        for (SpendingEntity s : spendings) {
            String category = Optional.ofNullable(s.getCategory()).orElse("기타");
            BigDecimal amount = Optional.ofNullable(s.getAmount()).orElse(BigDecimal.ZERO);

            spendingByCategory.merge(category, amount, BigDecimal::add);
            totalSpending = totalSpending.add(amount);

            descriptionByCategory
                    .computeIfAbsent(category, k -> new ArrayList<>())
                    .add(Optional.ofNullable(s.getDescription()).orElse("기재 없음"));
        }

        // 📈 4. 예산과 비교 데이터 구성
        Map<String, BigDecimal> budgetByCategory = new HashMap<>();
        Map<String, Map<String, Object>> budgetVsSpending = new HashMap<>();
        List<String> overBudgetCategories = new ArrayList<>();
        BigDecimal totalBudget = BigDecimal.ZERO;

        for (BudgetEntity b : budgets) {
            String category = b.getCategory();
            BigDecimal total = Optional.ofNullable(b.getTotalBudget()).orElse(BigDecimal.ZERO);
            BigDecimal used = spendingByCategory.getOrDefault(category, BigDecimal.ZERO);

            BigDecimal percent = total.compareTo(BigDecimal.ZERO) > 0
                    ? used.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            if (percent.compareTo(BigDecimal.valueOf(100)) > 0) {
                overBudgetCategories.add(category);
            }

            budgetByCategory.put(category, total);
            totalBudget = totalBudget.add(total);

            Map<String, Object> details = new HashMap<>();
            details.put("used", used);
            details.put("percent", percent.setScale(1, RoundingMode.HALF_UP));
            budgetVsSpending.put(category, details);
        }

        // 🧠 5. 설명 요약 (상위 3개)
        Map<String, String> descriptionSummary = new HashMap<>();
        descriptionByCategory.forEach((cat, descList) -> {
            String summary = descList.stream()
                    .filter(Objects::nonNull)
                    .limit(3)
                    .collect(Collectors.joining(", "));
            descriptionSummary.put(cat, summary);
        });

        // 📤 6. 결과 조립
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user_id", "masked");
        result.put("month", yearMonth.toString());
        result.put("summary", Map.of(
                "total_spending", totalSpending,
                "total_budget", totalBudget,
                "over_budget_categories", overBudgetCategories
        ));
        result.put("spending_by_category", spendingByCategory);
        result.put("budget_by_category", budgetByCategory);
        result.put("budget_vs_spending", budgetVsSpending);
        result.put("description_summary", descriptionSummary);

        return result;
    }
}
