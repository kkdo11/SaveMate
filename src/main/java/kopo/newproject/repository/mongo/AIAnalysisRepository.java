package kopo.newproject.repository.mongo;

import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AIAnalysisRepository extends MongoRepository<AIAnalysisEntity, String> {
    List<AIAnalysisEntity> findByUserIdAndMonth(String userId, String month);

    void deleteByUserIdAndMonth(String userId, String month);

    java.util.Optional<AIAnalysisEntity> findTopByUserIdOrderByCreatedAtDesc(String userId);
    List<AIAnalysisEntity> findByUserIdAndMonthOrderByCreatedAtDesc(String userId, String month);
    java.util.Optional<AIAnalysisEntity> findByIdAndUserId(String id, String userId);
}
