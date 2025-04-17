package kopo.newproject.repository.mongo;

import kopo.newproject.repository.entity.mongo.SpendingEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.YearMonth;
import java.util.List;

public interface SpendingRepository extends MongoRepository<SpendingEntity, ObjectId> {

    // 사용자별, 월별, 카테고리별 지출 내역 조회
    List<SpendingEntity> findByUserIdAndMonthAndCategory(String userId, YearMonth month, String category);

    // 사용자별, 월별 지출 내역 조회
    List<SpendingEntity> findByUserIdAndMonth(String userId, YearMonth month);

    // 사용자별, 카테고리별 지출 내역 조회
    List<SpendingEntity> findByUserIdAndCategory(String userId, String category);


    // 사용자별 지출 내역 조회
    List<SpendingEntity> findByUserId(String userId);
}
