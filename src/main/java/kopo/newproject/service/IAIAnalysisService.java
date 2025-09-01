package kopo.newproject.service;

import kopo.newproject.dto.PredictionDTO;
import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import java.util.List;
import java.util.Map;
import java.time.YearMonth;

public interface IAIAnalysisService {

    String analyze(String userId, String yearMonth);

    AIAnalysisEntity getAnalysisByMonth(String userId, String yearMonth);

    void deleteAnalysisByMonth(String userId, String yearMonth);

    Object getLatestAnalysis(String userId);

    List<AIAnalysisEntity> getAnalysisHistory(String userId, String yearMonth);

    Object getAnalysisById(String userId, String analysisId);

    Object compareAnalysis(String userId, String analysisId1, String analysisId2);

    PredictionDTO predictNextMonthSpending(String userId);

}