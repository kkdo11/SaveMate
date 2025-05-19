package kopo.newproject.service.impl;

import kopo.newproject.dto.SpendingRequest;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.repository.mongo.SpendingRepository;
import kopo.newproject.service.ISpendingService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("SpendingService")
@RequiredArgsConstructor
public class SpendingService implements ISpendingService {

    private final SpendingRepository spendingRepository;

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
                .month(YearMonth.from(request.getDate())) // YearMonth 타입으로 설정
                .build();

        return spendingRepository.save(entity);
    }

    @Override
    public List<SpendingEntity> getSpendings(String userId, YearMonth month, String category) {
        if (month != null && category != null) {
            return spendingRepository.findByUserIdAndMonthAndCategory(userId, month, category);
        } else if (month != null) {
            return spendingRepository.findByUserIdAndMonth(userId, month);
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
                entity.setMonth(YearMonth.from(request.getDate()));
                spendingRepository.save(entity);

                return true;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return false;
    }

    //카테고리별 월간 사용액 합산
    public BigDecimal calculateMonthlySpendingSum(String userId, int year, int month, String category) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return spendingRepository.sumAmountByUserIdAndMonthAndCategory(userId, yearMonth, category);
    }

    @Override
    public Map<String, Integer> getTotalAmountGroupedByCategory(String userId) {
        List<SpendingEntity> spendings = spendingRepository.findByUserId(userId);

        Map<String, Integer> result = new HashMap<>();
        for (SpendingEntity s : spendings) {
            String category = s.getCategory();
            int amount = s.getAmount().intValue(); // ✅ BigDecimal → int 변환
            result.put(category, result.getOrDefault(category, 0) + amount);
        }
        return result;
    }


    @Override
    public Map<String, Integer> getTotalSpendingByMonth(String userId) {
        List<SpendingEntity> spendings = spendingRepository.findByUserId(userId);

        Map<String, Integer> result = new HashMap<>();
        for (SpendingEntity s : spendings) {
            String monthKey = s.getMonth().toString(); // ex) "2025-03"
            int amount = s.getAmount().intValue(); // ✅ BigDecimal → int 변환
            result.put(monthKey, result.getOrDefault(monthKey, 0) + amount);
        }
        return result;
    }



}
