package kopo.newproject.service;

import kopo.newproject.dto.PredictionDTO;
import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import java.util.List;

/**
 * AI 기반 재무 분석과 관련된 비즈니스 로직의 명세(Contract)를 정의하는 인터페이스.
 * 이 인터페이스는 소비 데이터 분석, 미래 소비 예측, 분석 결과 조회/관리 등의 기능을 포함합니다.
 */
public interface IAIAnalysisService {

    /**
     * 특정 사용자의 특정 월 소비 데이터에 대한 AI 분석을 수행하고, 그 결과를 문자열 형태로 반환합니다.
     *
     * @param userId    분석을 요청한 사용자 ID
     * @param yearMonth 분석할 대상 연월 (예: "2025-08")
     * @return AI가 생성한 분석 리포트 (주로 JSON 또는 텍스트 형태)
     */
    String analyze(String userId, String yearMonth);

    /**
     * 특정 월에 대해 가장 최근에 저장된 분석 결과를 조회합니다.
     *
     * @param userId    사용자 ID
     * @param yearMonth 조회할 연월
     * @return 해당 월의 분석 결과 엔티티. 없을 경우 null을 반환할 수 있습니다.
     */
    AIAnalysisEntity getAnalysisByMonth(String userId, String yearMonth);

    /**
     * 특정 월의 분석 결과를 삭제합니다.
     *
     * @param userId    사용자 ID
     * @param yearMonth 삭제할 연월
     */
    void deleteAnalysisByMonth(String userId, String yearMonth);

    /**
     * 특정 사용자의 가장 최근 분석 결과를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 가장 최근의 분석 결과 객체. (NOTE: 반환 타입을 AIAnalysisEntity로 구체화하는 것을 권장합니다.)
     */
    Object getLatestAnalysis(String userId);

    /**
     * 특정 월의 분석 히스토리(이력) 목록을 조회합니다.
     *
     * @param userId    사용자 ID
     * @param yearMonth 조회할 연월
     * @return 해당 월의 분석 결과 엔티티 목록
     */
    List<AIAnalysisEntity> getAnalysisHistory(String userId, String yearMonth);

    /**
     * 고유 분석 ID를 통해 특정 분석 결과를 조회합니다.
     *
     * @param userId     사용자 ID
     * @param analysisId 조회할 분석의 고유 ID
     * @return ID에 해당하는 분석 결과 객체. (NOTE: 반환 타입을 AIAnalysisEntity로 구체화하는 것을 권장합니다.)
     */
    Object getAnalysisById(String userId, String analysisId);

    /**
     * 두 개의 다른 분석 결과를 비교하는 로직을 수행합니다.
     *
     * @param userId      사용자 ID
     * @param analysisId1 비교할 첫 번째 분석 ID
     * @param analysisId2 비교할 두 번째 분석 ID
     * @return 두 분석의 비교 결과 객체. (NOTE: 비교 결과를 담는 별도의 DTO를 정의하는 것을 권장합니다.)
     */
    Object compareAnalysis(String userId, String analysisId1, String analysisId2);

    /**
     * 사용자의 과거 소비 패턴을 기반으로 다음 달의 총 소비 금액을 예측합니다.
     *
     * @param userId 사용자 ID
     * @return 다음 달 소비 예측 데이터를 담은 DTO
     */
    PredictionDTO predictNextMonthSpending(String userId);

}
