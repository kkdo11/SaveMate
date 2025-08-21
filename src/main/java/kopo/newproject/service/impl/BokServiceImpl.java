
package kopo.newproject.service.impl;

import kopo.newproject.client.BokApiClient;
import kopo.newproject.dto.BokApiResponse;
import kopo.newproject.dto.StatisticRow;
import kopo.newproject.service.IBokService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BokServiceImpl implements IBokService {

    private final BokApiClient bokApiClient;

    @Value("${bok.api.key}")
    private String apiKey;

    private static final String STAT_CODE = "901Y009"; // 소비자물가지수(총지수, 2020=100)
    private static final String FORMAT = "json";
    private static final String LANG = "kr";
    private static final int START_COUNT = 1;
    private static final int END_COUNT = 100; // 최대 100개까지 조회
    private static final String PERIOD = "M"; // 월별 데이터

    @Override
    public List<StatisticRow> getCpiData(String startDate, String endDate) {
        log.info("Fetching CPI data from BOK API for period: {} - {}", startDate, endDate);
        try {
            BokApiResponse response = bokApiClient.getStatistics(
                    apiKey, FORMAT, LANG, START_COUNT, END_COUNT,
                    STAT_CODE, PERIOD, startDate, endDate
            );

            if (response != null && response.getStatisticSearch() != null && response.getStatisticSearch().getRow() != null) {
                log.info("Successfully fetched {} rows of data.", response.getStatisticSearch().getListTotalCount());
                // 최신순으로 정렬하여 반환
                response.getStatisticSearch().getRow().sort(Comparator.comparing(StatisticRow::getTime).reversed());
                return response.getStatisticSearch().getRow();
            }
        } catch (Exception e) {
            log.error("Failed to fetch CPI data from BOK API", e);
        }
        return Collections.emptyList();
    }

    @Override
    public double getLatestCpiGrowthRate() {
        log.info("Calculating latest CPI growth rate...");

        // 1. 조회 기간 설정 (최근 12개월)
        YearMonth currentMonth = YearMonth.now();
        YearMonth twelveMonthsAgo = currentMonth.minusMonths(11); // 현재 월 포함 12개월 전
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");

        String endDate = currentMonth.format(formatter);
        String startDate = twelveMonthsAgo.format(formatter);

        // 2. CPI 데이터 조회 (최근 12개월)
        List<StatisticRow> cpiData = getCpiData(startDate, endDate);

        // 3. 데이터 검증 및 등락률 계산
        if (cpiData == null || cpiData.size() < 2) {
            log.error("Cannot calculate growth rate: Not enough data points in the last 12 months (found {}).", cpiData != null ? cpiData.size() : 0);
            return 0.0; // 데이터 부족 시, 성장률 0.0으로 처리
        }

        try {
            // getCpiData()에서 이미 최신순으로 정렬되어 있으므로, 첫 번째와 두 번째 데이터 사용
            StatisticRow latestData = cpiData.get(0);
            StatisticRow previousData = cpiData.get(1);

            // 데이터 값 추출 및 BigDecimal로 변환
            BigDecimal latestValue = new BigDecimal(latestData.getDataValue());
            BigDecimal previousValue = new BigDecimal(previousData.getDataValue());

            log.info("Latest CPI ({}) value: {}, Previous CPI ({}) value: {}",
                    latestData.getTime(), latestValue, previousData.getTime(), previousValue);

            // 4. 등락률 계산
            if (previousValue.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("Previous month's CPI is zero, cannot calculate growth rate.");
                return 0.0;
            }

            BigDecimal growthRate = latestValue.subtract(previousValue)
                    .divide(previousValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));

            double finalRate = growthRate.setScale(2, RoundingMode.HALF_UP).doubleValue();
            log.info("Calculated CPI growth rate: {}%", finalRate);
            return finalRate;

        } catch (NumberFormatException e) {
            log.error("Error parsing CPI data value to number.", e);
            return 0.0;
        } catch (Exception e) {
            log.error("An unexpected error occurred during CPI growth rate calculation.", e);
            return 0.0;
        }
    }
}
