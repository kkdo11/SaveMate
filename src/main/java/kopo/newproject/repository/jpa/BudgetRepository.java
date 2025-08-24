package kopo.newproject.repository.jpa;

import kopo.newproject.repository.entity.jpa.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {@link BudgetEntity}에 대한 데이터 접근(Repository) 인터페이스.
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속받아 기본적인 CRUD(Create, Read, Update, Delete)
 * 및 페이징, 정렬 기능을 자동으로 제공합니다.
 * <p>
 * {@code @Repository} - 이 인터페이스가 Spring의 데이터 접근 계층 컴포넌트임을 나타냅니다.
 */
@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    /**
     * 특정 사용자의 모든 예산 목록을 조회합니다.
     *
     * @param userId 예산을 조회할 사용자 ID
     * @return 해당 사용자의 모든 예산 엔티티 목록
     */
    List<BudgetEntity> findAllByUserId(String userId);

    /**
     * 특정 사용자의 특정 연도, 월에 해당하는 예산 중 첫 번째 항목을 조회합니다.
     * <p>
     * (NOTE: 한 사용자가 같은 연월에 여러 카테고리의 예산을 가질 수 있으므로,
     * 이 메소드는 단일 결과를 기대하는 경우에만 사용해야 합니다.
     * 해당 연월의 모든 예산을 조회하려면 {@link #findAllByUserIdAndYearAndMonth(String, int, int)}를 사용하세요.)
     *
     * @param userId 예산을 조회할 사용자 ID
     * @param year   조회할 연도
     * @param month  조회할 월
     * @return 조건에 맞는 첫 번째 예산 엔티티 (Optional)
     */
    Optional<BudgetEntity> findByUserIdAndYearAndMonth(String userId, int year, int month);

    /**
     * 특정 사용자의 특정 연도, 월에 해당하는 모든 예산 목록을 조회합니다.
     *
     * @param userId 예산을 조회할 사용자 ID
     * @param year   조회할 연도
     * @param month  조회할 월
     * @return 조건에 맞는 모든 예산 엔티티 목록
     */
    List<BudgetEntity> findAllByUserIdAndYearAndMonth(String userId, int year, int month);

    /**
     * 특정 사용자의 특정 예산 ID에 해당하는 예산 정보를 조회합니다.
     * <p>
     * 이 메소드는 예산 ID와 사용자 ID를 모두 사용하여, 해당 예산이 요청한 사용자의 것인지 확인하며 조회합니다.
     *
     * @param userId   예산을 조회할 사용자 ID
     * @param budgetId 조회할 예산의 고유 ID
     * @return 조건에 맞는 예산 엔티티 (Optional)
     */
    Optional<BudgetEntity> findByUserIdAndBudgetId(String userId, Long budgetId);

    /**
     * ❗️(주의)❗️ 특정 월에 해당하는 모든 사용자의 예산 목록을 조회합니다.
     * <p>
     * 이 메소드는 사용자 ID로 필터링하지 않으므로, 모든 사용자의 예산 데이터가 반환됩니다.
     * 관리자 기능 등 매우 제한적이고 보안이 확보된 경우에만 사용해야 합니다.
     *
     * @param month 조회할 월
     * @return 해당 월의 모든 예산 엔티티 목록
     */
    List<BudgetEntity> findByMonth(int month);

    /**
     * ❗️(주의)❗️ 특정 카테고리에 해당하는 모든 사용자의 예산 목록을 조회합니다.
     * <p>
     * 이 메소드는 사용자 ID로 필터링하지 않으므로, 모든 사용자의 예산 데이터가 반환됩니다.
     * 관리자 기능 등 매우 제한적이고 보안이 확보된 경우에만 사용해야 합니다.
     *
     * @param category 조회할 카테고리
     * @return 해당 카테고리의 모든 예산 엔티티 목록
     */
    List<BudgetEntity> findByCategory(String category);

    /**
     * ❗️(주의)❗️ 특정 월과 카테고리에 해당하는 모든 사용자의 예산 목록을 조회합니다.
     * <p>
     * 이 메소드는 사용자 ID로 필터링하지 않으므로, 모든 사용자의 예산 데이터가 반환됩니다.
     * 관리자 기능 등 매우 제한적이고 보안이 확보된 경우에만 사용해야 합니다.
     *
     * @param month    조회할 월
     * @param category 조회할 카테고리
     * @return 조건에 맞는 모든 예산 엔티티 목록
     */
    List<BudgetEntity> findByMonthAndCategory(int month, String category);
}