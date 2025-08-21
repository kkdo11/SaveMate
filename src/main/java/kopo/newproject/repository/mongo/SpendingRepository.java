package kopo.newproject.repository.mongo;

import kopo.newproject.dto.SpendingTotalDTO;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface SpendingRepository extends MongoRepository<SpendingEntity, ObjectId> {

    // 사용자별, 월별, 카테고리별 지출 내역 조회
    List<SpendingEntity> findByUserIdAndMonthAndCategory(String userId, YearMonth month, String category);

    // 사용자별, 월별 지출 내역 조회
    List<SpendingEntity> findByUserIdAndMonth(String userId, YearMonth month);

    // 사용자별, 카테고리별 지출 내역 조회
    List<SpendingEntity> findByUserIdAndCategory(String userId, String category);


    // 사용자별 지출 내역 조회
    List<SpendingEntity> findByUserId(String userId);

    // 💡 사용자 + 월 + 카테고리 조건에 맞는 지출 총합 계산
    default BigDecimal sumAmountByUserIdAndMonthAndCategory(String userId, YearMonth month, String category) {
        List<SpendingEntity> list = findByUserIdAndMonthAndCategory(userId, month, category);
        return list.stream()
                .map(e -> Optional.ofNullable(e.getAmount()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, (acc, amount) -> acc.add(amount));
    }

    @Aggregation(pipeline = {
        "{$match: { 'userId': ?0, 'category': ?1, 'month': ?2 }}",
        "{$group: { '_id': null, 'total': { $sum: '$amount' } }}"
    })
    SpendingTotalDTO sumAmountByUserIdAndCategoryAndMonth(String userId, String category, String month);

    List<SpendingEntity> findByUserIdAndDateBetween(String userId, LocalDate start, LocalDate end);



}