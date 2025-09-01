package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
    List<BudgetEntity> findAllByUserId(String userId);

    // 유지: 기존 단건 조회용
    Optional<BudgetEntity> findByUserIdAndYearAndMonth(String userId, int year, int month);

    // 추가: 복수 건 조회용 (AnalysisPreprocessorService 용)
    List<BudgetEntity> findAllByUserIdAndYearAndMonth(String userId, int year, int month);

    Optional<BudgetEntity> findByUserIdAndBudgetId(String userId, Long id);
    List<BudgetEntity> findByMonth(int month);
    List<BudgetEntity> findByCategory(String category);
    List<BudgetEntity> findByMonthAndCategory(int month, String category);




}
