package kopo.newproject.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * 월간 재무 리포트의 모든 데이터를 담는 DTO(Data Transfer Object).
 * <p>
 * 이 DTO는 사용자에게 발송될 월간 소비 리포트의 내용을 구성하는 데 사용됩니다.
 * 다양한 재무 데이터(소비, 예산, 목표, AI 분석 결과)를 통합하여 제공합니다.
 * <p>
 * {@code @Getter} - Lombok 어노테이션으로, 모든 필드에 대한 getter 메소드를 자동으로 생성합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 * 모든 필드가 {@code final}로 선언되어 불변(immutable) 객체임을 나타냅니다.
 */
@Getter
@Builder
public class MonthlyReportDTO {

    /**
     * 리포트 대상 사용자의 이름.
     */
    private final String userName;

    /**
     * 리포트 대상 사용자의 이메일 주소.
     */
    private final String userEmail;

    /**
     * 리포트가 생성된 연월.
     */
    private final YearMonth reportMonth;

    /**
     * 해당 월의 총 지출 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     */
    private final BigDecimal totalSpending;

    /**
     * 이전 월의 총 지출 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     */
    private final BigDecimal previousMonthTotalSpending;

    /**
     * 이전 월 대비 현재 월의 지출 변화율 (백분율).
     * {@code BigDecimal}을 사용하여 정확한 계산을 보장합니다.
     */
    private final BigDecimal spendingChangePercentage;

    /**
     * 해당 월의 총 예산 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     */
    private final BigDecimal totalBudget;

    /**
     * 예산 달성률 (총 지출액 / 총 예산액 * 100%).
     * {@code BigDecimal}을 사용하여 정확한 계산을 보장합니다.
     */
    private final BigDecimal budgetAchievementRate;

    /**
     * 지출액 기준으로 상위 카테고리 목록.
     * 각 카테고리의 지출액과 전체 지출액 대비 비율을 포함합니다.
     * {@link CategorySpendingDTO} 리스트로 구성됩니다.
     */
    private final List<CategorySpendingDTO> topSpendingCategories;

    /**
     * 카테고리별 지출 금액을 담은 Map.
     * Key: 카테고리명 (String), Value: 해당 카테고리의 총 지출액 (BigDecimal).
     */
    private final Map<String, BigDecimal> spendingByCategory;

    /**
     * 사용자의 재정 목표 달성 현황 목록.
     * 각 목표의 이름, 목표 금액, 저축액, 달성률 등을 포함합니다.
     * {@link GoalStatusDTO} 리스트로 구성됩니다.
     */
    private final List<GoalStatusDTO> goalStatuses;

    /**
     * AI 분석 결과에서 추출된 요약 텍스트.
     * 사용자의 소비 습관에 대한 AI의 인사이트를 제공합니다.
     */
    private final String aiSummary;

    /**
     * 월간 리포트 내에서 카테고리별 지출 상세 정보를 나타내는 DTO.
     */
    @Getter
    @Builder
    public static class CategorySpendingDTO {
        /**
         * 지출 카테고리명.
         */
        private final String category;
        /**
         * 해당 카테고리의 총 지출 금액.
         * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
         */
        private final BigDecimal amount;
        /**
         * 해당 카테고리 지출이 전체 지출에서 차지하는 비율 (백분율).
         */
        private final double percentage;
    }

    /**
     * 월간 리포트 내에서 재정 목표의 달성 현황을 나타내는 DTO.
     */
    @Getter
    @Builder
    public static class GoalStatusDTO {
        /**
         * 재정 목표의 이름.
         */
        private final String goalName;
        /**
         * 목표 달성 금액.
         * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
         */
        private final BigDecimal targetAmount;
        /**
         * 현재까지 저축된 금액.
         * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
         */
        private final BigDecimal savedAmount;
        /**
         * 목표 달성률 (백분율).
         */
        private final double achievementRate;
        /**
         * 목표 달성 여부.
         */
        private final boolean isAchieved;
    }
}