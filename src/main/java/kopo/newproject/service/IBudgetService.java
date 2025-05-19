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

    Map<String, Integer> getTotalBudgetByMonth(String userId);

}
