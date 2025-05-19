package kopo.newproject.repository.mongo;

import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AIAnalysisRepository extends MongoRepository<AIAnalysisEntity, String> {
    List<AIAnalysisEntity> findByUserIdAndMonth(String userId, String month);

    void deleteByUserIdAndMonth(String userId, String month);
}

