package kopo.newproject.service.impl;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.entity.mongo.AggregatedSpendingEntity;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.repository.jpa.UserInfoRepository;
import kopo.newproject.repository.mongo.AggregatedSpendingRepository;
import kopo.newproject.repository.mongo.SpendingRepository;
import kopo.newproject.service.IAggregatedSpendingService;
import kopo.newproject.util.AgeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 내부 사용자 데이터 기반 소비 비교 관련 서비스 구현체
 */
@RequiredArgsConstructor
@Service
public class AggregatedSpendingServiceImpl implements IAggregatedSpendingService {

    private static final Logger log = LoggerFactory.getLogger(AggregatedSpendingServiceImpl.class);

    private final UserInfoRepository userInfoRepository;
    private final SpendingRepository spendingRepository;
    private final AggregatedSpendingRepository aggregatedSpendingRepository;




    /**
     * 전체 사용자의 현재 월 소비 데이터를 집계하여 성별/연령대별 평균을 계산하고 MongoDB에 저장합니다.
     * 매일 새벽 4시에 자동으로 실행됩니다.
     * @return 저장된 집계 데이터 리스트
     * @throws Exception 데이터 처리 중 발생할 수 있는 예외
     */
    @Scheduled(cron = "0 0 4 * * ?") // 매일 새벽 4시에 실행
    @Override
    public List<AggregatedSpendingEntity> aggregateAndCacheSpendingData() throws Exception {
        log.info("START: aggregateAndCacheSpendingData");

        // 1. 집계 기준 월 설정 (현재 월)
        YearMonth currentMonth = YearMonth.now();
        log.info("Aggregation target month: {}", currentMonth);

        // 2. 모든 사용자 정보 조회
        List<UserInfoEntity> users = userInfoRepository.findAll();
        log.info("Found {} total users.", users.size());

        // 3. 그룹별(성별+연령대) 데이터 집계를 위한 자료구조 초기화
        // Key: "성별_연령대" (e.g., "M_20대"), Value: { "카테고리": { "total": 총액, "count": 횟수 } }
        Map<String, Map<String, BigDecimal>> groupCategoryTotalSpending = new HashMap<>();
        Map<String, Long> groupUserCount = new HashMap<>();

        // 4. 각 사용자별로 데이터 처리
        for (UserInfoEntity user : users) {
            try {
                String userId = user.getUserId();
                String gender = user.getGender();
                String birthDate = user.getBirthDate();

                log.debug("Processing user: {} (Gender: {}, BirthDate: {})", userId, gender, birthDate);

                // 4-1. 필수 정보(성별, 생년월일) 누락 시 집계에서 제외
                if (gender == null || birthDate == null || gender.isEmpty() || birthDate.isEmpty()) {
                    log.warn("User {} is missing profile information (gender or birthDate). Skipping.", userId);
                    continue;
                }

                // 4-2. 연령 및 연령대 계산
                int age = AgeCalculator.calculateAge(birthDate);
                if (age == -1) {
                    log.warn("Failed to calculate age for user {}. Skipping.", userId);
                    continue;
                }
                String ageGroup = AgeCalculator.getAgeGroup(age);
                log.debug("User {} belongs to age group: {}", userId, ageGroup);

                // 4-3. 그룹 키 생성
                String groupKey = gender + "_" + ageGroup; // e.g., "M_20대"

                // 4-4. 해당 사용자의 현재 월 지출 데이터 조회
                List<SpendingEntity> userSpendings = spendingRepository.findByUserIdAndDateBetween(userId, currentMonth.atDay(1), currentMonth.atEndOfMonth().plusDays(1));
                if (userSpendings.isEmpty()) {
                    log.debug("User {} has no spending data for {}. Skipping.", userId, currentMonth);
                    continue; // 이번 달 지출이 없으면 다음 사용자로
                }
                log.debug("Found {} spending records for user {} in {}", userSpendings.size(), userId, currentMonth);

                // 4-5. 그룹의 사용자 수 카운트 (지출이 있는 사용자만 카운트)
                groupUserCount.put(groupKey, groupUserCount.getOrDefault(groupKey, 0L) + 1);

                // 4-6. 사용자별 카테고리 지출 합산 및 그룹 총계에 반영
                userSpendings.forEach(spending -> {
                    String category = spending.getCategory();
                    BigDecimal amount = spending.getAmount();

                    // 그룹의 카테고리별 총 지출액 업데이트
                    Map<String, BigDecimal> categorySpending = groupCategoryTotalSpending.computeIfAbsent(groupKey, k -> new HashMap<>());
                    categorySpending.put(category, categorySpending.getOrDefault(category, BigDecimal.ZERO).add(amount));
                });
                log.debug("Added user {}'s spending to group {}. Current totals: {}", userId, groupKey, groupCategoryTotalSpending.get(groupKey));

            } catch (Exception e) {
                log.error("Error processing user data for userId: {}. Skipping this user.", user.getUserId(), e);
                // 개별 사용자 오류가 전체 배치에 영향을 주지 않도록 루프 계속
            }
        }

        log.info("Finished processing all users. Aggregating results for {} groups.", groupCategoryTotalSpending.size());

        // 5. 그룹별 평균 계산 및 최종 데이터 생성
        List<AggregatedSpendingEntity> aggregatedDataList = new ArrayList<>();
        for (Map.Entry<String, Map<String, BigDecimal>> entry : groupCategoryTotalSpending.entrySet()) {
            String groupKey = entry.getKey();
            String[] parts = groupKey.split("_");
            String gender = parts[0];
            String ageGroup = parts[1];
            long userCountInGroup = groupUserCount.getOrDefault(groupKey, 0L);

            if (userCountInGroup == 0) {
                log.warn("Group {} has 0 users with spending. Skipping average calculation.", groupKey);
                continue;
            }

            log.debug("Calculating averages for group {} (Gender: {}, AgeGroup: {}, UserCount: {})", groupKey, gender, ageGroup, userCountInGroup);

            // 5-1. 카테고리별 평균 지출 계산
            Map<String, BigDecimal> categoryAverageSpending = new HashMap<>();
            entry.getValue().forEach((category, totalAmount) -> {
                BigDecimal averageAmount = totalAmount.divide(BigDecimal.valueOf(userCountInGroup), 2, RoundingMode.HALF_UP);
                categoryAverageSpending.put(category, averageAmount);
            });
            log.debug("Calculated category averages for group {}: {}", groupKey, categoryAverageSpending);

            // 5-2. 최종 집계 엔티티 생성
            AggregatedSpendingEntity aggregatedSpending = AggregatedSpendingEntity.builder()
                    .id(currentMonth.toString() + "_" + groupKey) // 고유 ID 생성 (e.g., "2025-07_M_20대")
                    .month(currentMonth)
                    .gender(gender)
                    .ageGroup(ageGroup)
                    .categoryAverageSpending(categoryAverageSpending)
                    .userCount(userCountInGroup)
                    .build();
            aggregatedDataList.add(aggregatedSpending);
        }

        // 6. 기존 집계 데이터 삭제 후 새로 저장 (멱등성 보장)
        if (!aggregatedDataList.isEmpty()) {
            // 특정 월의 데이터만 삭제하여 다른 월의 데이터에 영향을 주지 않도록 수정
            List<AggregatedSpendingEntity> existingDataForMonth = aggregatedSpendingRepository.findByMonth(currentMonth);
            if (!existingDataForMonth.isEmpty()) {
                aggregatedSpendingRepository.deleteAll(existingDataForMonth);
                log.info("Deleted {} existing aggregated data records for month {}.", existingDataForMonth.size(), currentMonth);
            }
            aggregatedSpendingRepository.saveAll(aggregatedDataList);
            log.info("Successfully saved {} new aggregated data records.", aggregatedDataList.size());
        } else {
            log.warn("No data to aggregate. Skipping database operations.");
        }

        log.info("END: aggregateAndCacheSpendingData");
        return aggregatedDataList;
    }

    /**
     * 특정 월, 성별, 연령대에 해당하는 집계된 소비 데이터를 조회합니다.
     * @param gender 조회할 성별
     * @param ageGroup 조회할 연령대
     * @return 조회된 집계 데이터 (없을 경우 null)
     * @throws Exception 데이터 처리 중 발생할 수 있는 예외
     */
    @Override
    public AggregatedSpendingEntity getAggregatedSpendingByGroup(String gender, String ageGroup) throws Exception {
        log.info("START: getAggregatedSpendingByGroup - gender: {}, ageGroup: {}", gender, ageGroup);

        // 현재 월 기준으로 조회
        YearMonth currentMonth = YearMonth.now();
        String id = currentMonth.toString() + "_" + gender + "_" + ageGroup;

        // findById를 사용하여 더 효율적으로 조회
        Optional<AggregatedSpendingEntity> rEntity = aggregatedSpendingRepository.findById(id);

        if (rEntity.isPresent()) {
            log.info("SUCCESS: Found aggregated data with ID: {}", rEntity.get().getId());
        } else {
            log.warn("WARN: Aggregated data not found for ID: {}", id);
        }

        log.info("END: getAggregatedSpendingByGroup");
        return rEntity.orElse(null); // Optional에서 값을 꺼내 반환, 없으면 null 반환
    }
}