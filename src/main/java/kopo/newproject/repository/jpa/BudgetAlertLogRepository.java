package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.BudgetAlertLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetAlertLogRepository extends JpaRepository<BudgetAlertLogEntity, Long> {

    /**
     * 특정 사용자의 해당 월, 해당 카테고리에 대한 알림 발송 기록이 있는지 확인
     */
    boolean existsByUserIdAndYearAndMonthAndCategory(String userId, int year, int month, String category);
}
