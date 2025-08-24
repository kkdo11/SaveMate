package kopo.newproject.service;

import kopo.newproject.dto.SpendingRequest;
import kopo.newproject.repository.entity.mongo.SpendingEntity;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * 지출(Spending) 내역 관련 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 */
public interface ISpendingService {

    /**
     * 조건에 따라 지출 내역 목록을 조회합니다.
     *
     * @param userId   사용자 ID
     * @param month    조회할 연월 (Optional, null일 경우 전체 기간)
     * @param category 조회할 카테고리 (Optional, null일 경우 전체 카테고리)
     * @return 조건에 맞는 지출 엔티티 목록
     */
    List<SpendingEntity> getSpendings(String userId, YearMonth month, String category);

    /**
     * 고유 ID를 통해 특정 지출 내역 한 건을 조회합니다.
     * (중요: 구현체에서는 반드시 해당 id의 소유자가 userId와 일치하는지 확인해야 합니다.)
     *
     * @param userId 사용자 ID (권한 확인용)
     * @param id     조회할 지출 내역의 고유 ID
     * @return 조회된 지출 엔티티. 없거나 권한이 없으면 null.
     */
    SpendingEntity getSpendingById(String userId, String id);

    /**
     * 새로운 지출 내역을 저장합니다.
     *
     * @param userId  지출 내역을 생성할 사용자 ID
     * @param request 생성할 지출 내역 정보를 담은 DTO
     * @return 저장된 지출 엔티티
     */
    SpendingEntity saveSpending(String userId, SpendingRequest request);

    /**
     * 기존 지출 내역을 수정합니다.
     * (중요: 구현체에서는 반드시 해당 id의 소유자가 userId와 일치하는지 확인해야 합니다.)
     *
     * @param userId  사용자 ID (권한 확인용)
     * @param id      수정할 지출 내역의 고유 ID
     * @param request 수정할 지출 정보를 담은 DTO
     * @return 성공 여부
     */
    boolean updateSpending(String userId, String id, SpendingRequest request);

    /**
     * 특정 지출 내역을 삭제합니다.
     * (중요: 구현체에서는 반드시 해당 id의 소유자가 userId와 일치하는지 확인해야 합니다.)
     *
     * @param userId 사용자 ID (권한 확인용)
     * @param id     삭제할 지출 내역의 고유 ID
     * @return 성공 여부
     */
    boolean deleteSpending(String userId, String id);

    /**
     * 대시보드용으로, 특정 사용자의 *전체 기간*에 대한 카테고리별 총 지출액을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return Key: 카테고리명, Value: 해당 카테고리의 총 지출액을 담은 Map
     */
    Map<String, Integer> getTotalAmountGroupedByCategory(String userId);

    /**
     * 대시보드용으로, 특정 사용자의 월별 총 지출액을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return Key: "YYYY-MM" 형식의 연월, Value: 해당 월의 총 지출액을 담은 Map
     */
    Map<String, Integer> getTotalSpendingByMonth(String userId);

    /**
     * 특정 사용자의 특정 월, 특정 카테고리에 대한 지출 합계를 계산합니다.
     *
     * @param userId   사용자 ID
     * @param year     조회할 연도
     * @param month    조회할 월
     * @param category 조회할 카테고리
     * @return 계산된 지출 합계
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    BigDecimal calculateMonthlySpendingSum(String userId, int year, int month, String category) throws Exception;

    /**
     * 월간 리포트용으로, 특정 사용자의 *특정 월*에 대한 카테고리별 지출액을 조회합니다.
     *
     * @param userId      사용자 ID
     * @param reportMonth 조회할 연월
     * @return Key: 카테고리명, Value: 해당 카테고리의 지출액을 담은 Map
     * @throws Exception 데이터 처리 중 예외 발생 시
     */
    Map<String, BigDecimal> getSpendingByCategory(String userId, YearMonth reportMonth) throws Exception;

}