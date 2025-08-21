package kopo.newproject.service;

import kopo.newproject.dto.BudgetDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;

import java.util.List;
import java.util.Map;

public interface IBudgetService {
    BudgetEntity createBudget(String userId, BudgetDTO dto);
    boolean updateBudget(String userId, Long budgetId, BudgetDTO dto);
    boolean deleteBudget(String userId, Long budgetId);
    BudgetEntity getBudgetByUserIdAndYearMonth(String userId, int year, int month);
    BudgetDTO getBudgetById(String userId, Long budgetId);

    List<BudgetEntity> findAll();
    List<BudgetEntity> findByMonthAndCategory(Integer month, String category);
    List<BudgetEntity> getBudgetsByUserId(String userId);
    List<BudgetEntity> getBudgetsByUserIdAndYearMonth(String userId, int year, int month);

    Map<String, Integer> getTotalBudgetByMonth(String userId);

    /**
     * 최신 소비자물가지수(CPI) 상승률을 반영하여 해당 사용자의 현재 월 모든 예산을 조정합니다.
     *
     * @param userId 예산을 조정할 사용자의 ID
     * @return 조정된 예산 엔티티 리스트
     */
    List<BudgetEntity> adjustAllBudgetsForCpi(String userId);

}
