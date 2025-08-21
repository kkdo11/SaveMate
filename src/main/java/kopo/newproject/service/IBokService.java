
package kopo.newproject.service;

import kopo.newproject.dto.StatisticRow;

import java.util.List;

public interface IBokService {

    /**
     * 한국은행 API를 통해 특정 기간의 소비자물가지수(CPI) 데이터를 가져옵니다.
     *
     * @param startDate 조회 시작일 (YYYYMM)
     * @param endDate   조회 종료일 (YYYYMM)
     * @return CPI 데이터 리스트
     */
    List<StatisticRow> getCpiData(String startDate, String endDate);

    /**
     * 가장 최신의 소비자물가지수(CPI) 등락률을 계산하여 반환합니다.
     * (예: 전월 대비 또는 전년 동월 대비)
     *
     * @return CPI 등락률 (소수점 두 자리까지)
     */
    double getLatestCpiGrowthRate();
}
