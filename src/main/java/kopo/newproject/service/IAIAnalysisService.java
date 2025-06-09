package kopo.newproject.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;

public interface IAIAnalysisService {
    String analyzeUserSpending(String userId, YearMonth yearMonth, Map<String, Object> preprocessedData);

    /**
     * 외부 호출용 간편 래퍼 (컨트롤러에서 사용)
     */
    String analyze(String userId, String yearMonth);

    // 월별 분석 조회
    List<AIAnalysisEntity> getAnalysisByMonth(String userId, String yearMonth);

    // 월별 분석 삭제
    void deleteAnalysisByMonth(String userId, String yearMonth);

    // 최신 분석 결과 조회
    Object getLatestAnalysis(String userId);

    // 분석 히스토리 조회
    List<AIAnalysisEntity> getAnalysisHistory(String userId, String yearMonth);

    // 특정 분석 결과 조회
    Object getAnalysisById(String userId, String analysisId);

    // 분석 결과 비교
    Object compareAnalysis(String userId, String analysisId1, String analysisId2);
}
