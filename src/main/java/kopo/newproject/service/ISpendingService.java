package kopo.newproject.service;

import kopo.newproject.dto.SpendingRequest;
import kopo.newproject.repository.entity.mongo.SpendingEntity;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

public interface ISpendingService {

    // 지출 내역을 월별 및 카테고리별로 필터링하여 가져오는 메서드
    List<SpendingEntity> getSpendings(String userId, YearMonth month, String category);

    SpendingEntity getSpendingById(String userId, String id);

    SpendingEntity saveSpending(String userId, SpendingRequest request);

    boolean updateSpending(String userId, String id, SpendingRequest request);

    boolean deleteSpending(String userId, String id);

    Map<String, Integer> getTotalAmountGroupedByCategory(String userId, YearMonth month);
    Map<String, Integer> getTotalSpendingByMonth(String userId, YearMonth from, YearMonth to);

    BigDecimal calculateMonthlySpendingSum(String userId, int year, int month, String category) throws Exception;

    Map<String, BigDecimal> getSpendingByCategory(String userId, YearMonth reportMonth) throws Exception;

}
