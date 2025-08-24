package kopo.newproject.service;

import kopo.newproject.dto.BudgetDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;

import java.util.List;
import java.util.Map;

/**
 * 예산(Budget) 관련 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 */
public interface IBudgetService {

    /**
     * 새로운 예산을 생성합니다.
     * @param userId    예산을 생성할 사용자 ID
     * @param dto       생성할 예산 정보를 담은 DTO
     * @return 생성된 예산 엔티티
     */
    BudgetEntity createBudget(String userId, BudgetDTO dto);

    /**
     * 기존 예산을 수정합니다.
     * @param userId    요청한 사용자 ID (권한 확인용)
     * @param budgetId  수정할 예산의 ID
     * @param dto       수정할 예산 정보를 담은 DTO
     * @return 성공 여부
     */
    boolean updateBudget(String userId, Long budgetId, BudgetDTO dto);

    /**
     * 특정 예산을 삭제합니다.
     * @param userId   요청한 사용자 ID (권한 확인용)
     * @param budgetId 삭제할 예산의 ID
     * @return 성공 여부
     */
    boolean deleteBudget(String userId, Long budgetId);

    /**
     * 특정 사용자의 특정 연/월 예산 정보를 조회합니다.
     * (NOTE: 이 메소드는 단일 엔티티를 반환하므로, 특정 카테고리의 예산 하나를 가져오는 등 매우 한정적인 경우에 사용될 것으로 보입니다.
     *  한 달의 전체 예산 목록을 가져오려면 getBudgetsByUserIdAndYearMonth를 사용하세요.)
     * @param userId 사용자 ID
     * @param year   조회할 연도
     * @param month  조회할 월
     * @return 조회된 예산 엔티티. 없을 경우 null.
     */
    BudgetEntity getBudgetByUserIdAndYearMonth(String userId, int year, int month);

    /**
     * 특정 예산 한 건을 ID로 조회합니다.
     * @param userId   요청한 사용자 ID (권한 확인용)
     * @param budgetId 조회할 예산의 ID
     * @return 조회된 예산 정보를 담은 DTO. 없을 경우 null.
     */
    BudgetDTO getBudgetById(String userId, Long budgetId);

    /**
     * ❗️(주의)❗️ 모든 사용자의 모든 예산 정보를 조회합니다.
     * 이 메소드는 사용자 ID로 필터링하지 않으므로, 관리자 기능 등 매우 제한적인 경우에만 사용해야 합니다.
     * @return 전체 예산 엔티티 목록
     */
    List<BudgetEntity> findAll();

    /**
     * ❗️(주의)❗️ 월과 카테고리로 예산 정보를 필터링하여 조회합니다.
     * 이 메소드는 사용자 ID로 필터링하지 않으므로, 전체 사용자 대상 통계 등 제한적인 경우에만 사용해야 합니다.
     * @param month    필터링할 월 (Optional)
     * @param category 필터링할 카테고리 (Optional)
     * @return 필터링된 예산 엔티티 목록
     */
    List<BudgetEntity> findByMonthAndCategory(Integer month, String category);

    /**
     * 특정 사용자의 모든 예산 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 전체 예산 엔티티 목록
     */
    List<BudgetEntity> getBudgetsByUserId(String userId);

    /**
     * 특정 사용자의 특정 연/월에 해당하는 모든 카테고리의 예산 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param year   조회할 연도
     * @param month  조회할 월
     * @return 해당 연/월의 예산 엔티티 목록
     */
    List<BudgetEntity> getBudgetsByUserIdAndYearMonth(String userId, int year, int month);

    /**
     * 대시보드 차트용으로, 특정 사용자의 월별 총 예산 합계를 조회합니다.
     * @param userId 사용자 ID
     * @return Key: "YYYY-MM" 형식의 연월, Value: 해당 월의 총 예산액을 담은 Map
     */
    Map<String, Integer> getTotalBudgetByMonth(String userId);

    /**
     * 최신 소비자물가지수(CPI) 상승률을 반영하여 해당 사용자의 현재 월 모든 예산을 조정합니다.
     *
     * @param userId 예산을 조정할 사용자의 ID
     * @return 조정된 예산 엔티티 리스트
     */
    List<BudgetEntity> adjustAllBudgetsForCpi(String userId);

}
