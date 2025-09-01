package kopo.newproject.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class MonthlyReportDTO {

    private final String userName;
    private final String userEmail;
    private final YearMonth reportMonth;
    private final BigDecimal totalSpending;
    private final BigDecimal previousMonthTotalSpending;
    private final BigDecimal spendingChangePercentage;
    private final BigDecimal totalBudget;
    private final BigDecimal budgetAchievementRate;
    private final List<CategorySpendingDTO> topSpendingCategories;
    private final Map<String, BigDecimal> spendingByCategory;
    private final List<GoalStatusDTO> goalStatuses;
    private final String aiSummary;

    @Getter
    @Builder
    public static class CategorySpendingDTO {
        private final String category;
        private final BigDecimal amount;
        private final double percentage;
    }

    @Getter
    @Builder
    public static class GoalStatusDTO {
        private final String goalName;
        private final BigDecimal targetAmount;
        private final BigDecimal savedAmount;
        private final double achievementRate;
        private final boolean isAchieved;
    }
}
