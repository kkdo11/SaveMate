package kopo.newproject.service.impl;

import kopo.newproject.dto.BudgetDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.repository.jpa.BudgetRepository;
import kopo.newproject.service.IBudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("BudgetService")
@RequiredArgsConstructor
public class BudgetService implements IBudgetService {

    private final BudgetRepository budgetRepository;
    private final SpendingService spendingService;


    // 예산 생성
    @Override
    public BudgetEntity createBudget(String userId, BudgetDTO dto) {
        BigDecimal used = spendingService.calculateMonthlySpendingSum(userId, dto.getYear(), dto.getMonth(), dto.getCategory());

        // BudgetService.createBudget()
        BudgetEntity entity = BudgetEntity.builder()
                .userId(userId)
                .year(dto.getYear())
                .month(dto.getMonth())
                .category(dto.getCategory())
                .totalBudget(dto.getTotalBudget() != null ? dto.getTotalBudget() : BigDecimal.ZERO)
                // .usedBudget(used) ❌ 제거!
                .build();

        return budgetRepository.save(entity);
    }


    @Override
    @Transactional
    public boolean updateBudget(String userId, Long budgetId, BudgetDTO dto) {
        Optional<BudgetEntity> optional = budgetRepository.findById(budgetId);

        if (optional.isPresent() && optional.get().getUserId().equals(userId)) {
            BudgetEntity entity = optional.get();

            BudgetEntity updatedEntity = BudgetEntity.builder()
                    .budgetId(entity.getBudgetId())
                    .userId(entity.getUserId())
                    .year(dto.getYear())
                    .month(dto.getMonth())
                    .category(dto.getCategory())
                    .totalBudget(dto.getTotalBudget())
                    .usedBudget(dto.getUsedBudget() != null ? dto.getUsedBudget() : BigDecimal.ZERO)
                    .build();

            budgetRepository.save(updatedEntity);
            return true;
        }

        return false;
    }


    // 예산 삭제
    @Override
    @Transactional
    public boolean deleteBudget(String userId, Long budgetId) {
        Optional<BudgetEntity> optional = budgetRepository.findById(budgetId);

        if (optional.isPresent() && optional.get().getUserId().equals(userId)) {
            budgetRepository.deleteById(budgetId);
            return true;
        }

        return false;
    }

    // 특정 연/월 예산 조회
    public BudgetEntity getBudgetByUserIdAndYearMonth(String userId, int year, int month) {
        Optional<BudgetEntity> optional = budgetRepository.findByUserIdAndYearAndMonth(userId, year, month);
        if (optional.isEmpty()) return null;

        BudgetEntity entity = optional.get();
        BigDecimal used = spendingService.calculateMonthlySpendingSum(userId, year, month, entity.getCategory());
        entity.setUsedBudget(used); // ✅ 실시간 사용액 계산 후 주입
        return entity;
    }


    @Override
    @Transactional(readOnly = true)
    public BudgetDTO getBudgetById(String userId, Long budgetId) {
        Optional<BudgetEntity> optional = budgetRepository.findByUserIdAndBudgetId(userId, budgetId);

        if (optional.isEmpty()) return null;

        BudgetEntity entity = optional.get();

        return BudgetDTO.builder()
                .budgetId(entity.getBudgetId())
                .userId(entity.getUserId())
                .year(entity.getYear())
                .month(entity.getMonth())
                .category(entity.getCategory())
                .totalBudget(entity.getTotalBudget())
                .usedBudget(entity.getUsedBudget())
                .remainingBudget(entity.getRemainingBudget())
                .build();
    }


    // 유저별 전체 예산 목록 조회
    public List<BudgetEntity> getBudgetsByUserId(String userId) {
        List<BudgetEntity> budgets = budgetRepository.findAllByUserId(userId);
        for (BudgetEntity b : budgets) {
            BigDecimal used = spendingService.calculateMonthlySpendingSum(
                    userId, b.getYear(), b.getMonth(), b.getCategory()
            );
            b.setUsedBudget(used);
        }
        return budgets;
    }



    @Override
    public List<BudgetEntity> findAll() {
        return budgetRepository.findAll();
    }

    @Override
    public List<BudgetEntity> findByMonthAndCategory(Integer month, String category) {
        if (month != null && category != null) {
            return budgetRepository.findByMonthAndCategory(month, category);
        } else if (month != null) {
            return budgetRepository.findByMonth(month);
        } else if (category != null) {
            return budgetRepository.findByCategory(category);
        } else {
            return budgetRepository.findAll();
        }
    }

    @Override
    public Map<String, Integer> getTotalBudgetByMonth(String userId) {
        List<BudgetEntity> budgets = budgetRepository.findAllByUserId(userId);

        Map<String, Integer> result = new HashMap<>();
        for (BudgetEntity b : budgets) {
            String key = String.format("%04d-%02d", b.getYear(), b.getMonth());
            int amount = b.getTotalBudget() != null ? b.getTotalBudget().intValue() : 0;
            result.put(key, result.getOrDefault(key, 0) + amount);
        }
        return result;
    }


}
