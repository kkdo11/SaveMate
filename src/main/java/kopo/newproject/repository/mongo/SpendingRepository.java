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

    // 사용자별, 카테고리별 지출 내역 조회
    List<SpendingEntity> findByUserIdAndCategory(String userId, String category);


    // 사용자별 지출 내역 조회
    List<SpendingEntity> findByUserId(String userId);


    @Aggregation(pipeline = {
            "{$match: { 'userId': ?0, 'date': { $gte: ?1, $lt: ?2 } }}",
            "{$group: { '_id': null, 'total': { $sum: '$amount' } }}"
    })
    SpendingTotalDTO sumAmountByDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    @Aggregation(pipeline = {
            "{$match: { 'userId': ?0, 'date': { $gte: ?1, $lt: ?2 }, 'category': ?3 }}",
            "{$group: { '_id': null, 'total': { $sum: '$amount' } }}"
    })
    SpendingTotalDTO sumAmountByDateBetweenAndCategory(String userId, LocalDate startDate, LocalDate endDate, String category);

    List<SpendingEntity> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    List<SpendingEntity> findByUserIdAndDateBetweenAndCategory(String userId, LocalDate startDate, LocalDate endDate, String category);



}