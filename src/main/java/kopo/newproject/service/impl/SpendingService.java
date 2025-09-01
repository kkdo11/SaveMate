package kopo.newproject.service.impl;

import kopo.newproject.dto.SpendingRequest;
import kopo.newproject.dto.SpendingTotalDTO;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.repository.mongo.SpendingRepository;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("SpendingService")
@RequiredArgsConstructor
public class SpendingService implements ISpendingService {

    private final SpendingRepository spendingRepository;
    private final MongoTemplate mongoTemplate; // MongoTemplate 주입

    // 지출 내역 저장
    @Override
    public SpendingEntity saveSpending(String userId, SpendingRequest request) {
        SpendingEntity entity = SpendingEntity.builder()
                .userId(userId)
                .name(request.getName())
                .date(request.getDate())
                .category(request.getCategory())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        return spendingRepository.save(entity);
    }

    @Override
    public List<SpendingEntity> getSpendings(String userId, YearMonth month, String category) {
        if (month != null) {
            LocalDate startOfMonth = month.atDay(1);
            LocalDate endOfMonth = month.atEndOfMonth().plusDays(1);

            if (category != null) {
                return spendingRepository.findByUserIdAndDateBetweenAndCategory(userId, startOfMonth, endOfMonth, category);
            } else {
                return spendingRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
            }
        } else if (category != null) {
            return spendingRepository.findByUserIdAndCategory(userId, category);
        } else {
            return spendingRepository.findByUserId(userId);
        }
    }

    @Override
    public SpendingEntity getSpendingById(String userId, String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            return spendingRepository.findById(objectId)
                    .filter(spending -> spending.getUserId().equals(userId))
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }



    //지출내역 삭제
    @Override
    public boolean deleteSpending(String userId, String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            return spendingRepository.findById(objectId)
                    .filter(spending -> spending.getUserId().equals(userId))
                    .map(spending -> {
                        spendingRepository.deleteById(objectId);
                        return true;
                    })
                    .orElse(false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    //지출내역 수정
    @Override
    public boolean updateSpending(String userId, String id, SpendingRequest request) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<SpendingEntity> spending = spendingRepository.findById(objectId);
            if (spending.isPresent() && spending.get().getUserId().equals(userId)) {
                SpendingEntity entity = spending.get();
                entity.setName(request.getName());
                entity.setDate(request.getDate());
                entity.setCategory(request.getCategory());
                entity.setAmount(request.getAmount());
                entity.setDescription(request.getDescription());
                spendingRepository.save(entity);

                return true;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return false;
    }

    //카테고리별 월간 사용액 합산
    @Override
    public BigDecimal calculateMonthlySpendingSum(String userId, int year, int month, String category) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);

        SpendingTotalDTO result;
        if (category != null) {
            result = spendingRepository.sumAmountByDateBetweenAndCategory(userId, startOfMonth, endOfMonth, category);
        } else {
            result = spendingRepository.sumAmountByDateBetween(userId, startOfMonth, endOfMonth);
        }
        return result != null ? result.total() : BigDecimal.ZERO;
    }

    @Override
    public Map<String, Integer> getTotalAmountGroupedByCategory(String userId, YearMonth month) {
        LocalDate startOfMonth = month.atDay(1);
        LocalDate endOfMonth = month.atEndOfMonth();

        List<SpendingEntity> spendings = spendingRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);

        Map<String, Integer> result = new HashMap<>();
        for (SpendingEntity s : spendings) {
            String category = s.getCategory();
            int amount = s.getAmount().intValue(); // ✅ BigDecimal → int 변환
            result.put(category, result.getOrDefault(category, 0) + amount);
        }
        return result;
    }


    @Override
    public Map<String, Integer> getTotalSpendingByMonth(String userId, YearMonth from, YearMonth to) {
        List<SpendingEntity> allSpendings = spendingRepository.findByUserId(userId);

        Map<String, Integer> result = new HashMap<>();
        allSpendings.stream()
                .filter(s -> {
                    YearMonth spendingMonth = YearMonth.from(s.getDate());
                    return !spendingMonth.isBefore(from) && !spendingMonth.isAfter(to);
                })
                .forEach(s -> {
                    String monthKey = YearMonth.from(s.getDate()).toString(); // ex) "2025-03"
                    int amount = s.getAmount().intValue(); // ✅ BigDecimal → int 변환
                    result.put(monthKey, result.getOrDefault(monthKey, 0) + amount);
                });
        return result;
    }

    // Helper class for aggregation result
    private static class CategorySpending {
        private String category;
        private BigDecimal total;

        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
    }

    @Override
    public Map<String, BigDecimal> getSpendingByCategory(String userId, YearMonth reportMonth) throws Exception {
        LocalDate startOfMonth = reportMonth.atDay(1);
        LocalDate endOfMonth = reportMonth.atEndOfMonth().plusDays(1);

        MatchOperation matchStage = Aggregation.match(
                new Criteria("userId").is(userId)
                        .and("date").gte(startOfMonth).lt(endOfMonth)
                        .and("category").nin(null, "") // 카테고리가 null이거나 빈 문자열이 아닌 경우만
        );
        GroupOperation groupStage = Aggregation.group("category")
                .sum("amount").as("total");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage);

        AggregationResults<CategorySpending> results = mongoTemplate.aggregate(
                aggregation, SpendingEntity.class, CategorySpending.class
        );

        return results.getMappedResults().stream()
                .collect(Collectors.toMap(CategorySpending::getCategory, CategorySpending::getTotal, (existing, replacement) -> existing.add(replacement))); // 중복 키 발생 시 값 합치기
    }



}
