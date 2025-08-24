
package kopo.newproject.service;

import kopo.newproject.dto.StatisticRow;

import java.util.List;

/**
 * 한국은행(BOK) 경제통계시스템(ECOS) API와 연동하여 경제 지표를 가져오는 서비스의 명세(Contract)를 정의하는 인터페이스.
 * <p>
 * 이 서비스는 주로 소비자물가지수(CPI)와 같은 데이터를 조회하여,
 * '물가상승률 반영 예산 자동 조정'과 같은 기능에 필요한 데이터를 제공하는 역할을 합니다.
 */
public interface IBokService {

    /**
     * 한국은행(BOK) API를 호출하여 특정 기간의 소비자물가지수(CPI) 데이터를 조회합니다.
     *
     * @param startDate 조회 시작일 (YYYYMM 형식, 예: "202401")
     * @param endDate   조회 종료일 (YYYYMM 형식, 예: "202501")
     * @return 조회된 기간 내의 CPI 데이터를 담고 있는 {@link StatisticRow} DTO 리스트
     */
    List<StatisticRow> getCpiData(String startDate, String endDate);

    /**
     * 가장 최신의 소비자물가지수(CPI) 등락률(물가상승률)을 계산하여 반환합니다.
     * <p>
     * 내부적으로 getCpiData를 호출하여 필요한 데이터를 가져온 후,
     * 전월 대비 또는 전년 동월 대비 등락률을 계산하는 로직을 수행합니다.
     *
     * @return 계산된 최신 CPI 등락률 (e.g., 3.1% 라면 3.1을 반환)
     */
    double getLatestCpiGrowthRate();
}
