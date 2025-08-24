package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.BudgetAlertLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link BudgetAlertLogEntity}에 대한 데이터 접근(Repository) 인터페이스.
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete)
 * 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * <p>
 * {@code @Repository} - 이 인터페이스가 Spring의 데이터 접근 계층 컴포넌트임을 나타냅니다.
 */
@Repository
public interface BudgetAlertLogRepository extends JpaRepository<BudgetAlertLogEntity, Long> {

    /**
     * 특정 사용자의 특정 연도, 월, 카테고리에 대한 예산 초과 알림 발송 기록이 존재하는지 확인합니다.
     * <p>
     * 이 메소드는 Spring Data JPA의 쿼리 메소드 파생(Query Method Derivation) 기능을 사용하여
     * 메소드 이름만으로 SQL 쿼리를 자동으로 생성합니다.
     * 주로 중복 알림 발송을 방지하는 로직에서 사용됩니다.
     *
     * @param userId   알림을 받은 사용자의 ID
     * @param year     알림이 발송된 예산의 연도
     * @param month    알림이 발송된 예산의 월
     * @param category 알림이 발송된 예산의 카테고리
     * @return 해당 조건의 알림 기록이 존재하면 true, 그렇지 않으면 false
     */
    boolean existsByUserIdAndYearAndMonthAndCategory(String userId, int year, int month, String category);
}