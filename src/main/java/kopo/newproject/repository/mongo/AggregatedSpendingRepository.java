package kopo.newproject.repository.mongo;

import kopo.newproject.repository.entity.mongo.AggregatedSpendingEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface AggregatedSpendingRepository extends MongoRepository<AggregatedSpendingEntity, String> {
    Optional<AggregatedSpendingEntity> findByMonthAndGenderAndAgeGroup(YearMonth month, String gender, String ageGroup);

    List<AggregatedSpendingEntity> findByMonth(YearMonth month);
}
