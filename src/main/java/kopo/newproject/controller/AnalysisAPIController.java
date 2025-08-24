package kopo.newproject.controller;

import kopo.newproject.dto.PredictionDTO;
import kopo.newproject.service.IAIAnalysisService;
import kopo.newproject.service.impl.AnalysisPreprocessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.Map;

/**
 * AI 기반 재무 분석 관련 API 요청을 처리하는 컨트롤러.
 * <p>
 * {@code @RestController} - 이 컨트롤러의 모든 메소드는 JSON/XML 등의 데이터(body)를 직접 반환합니다.
 * {@code @RequestMapping("/api/analysis")} - 이 컨트롤러의 모든 API는 '/api/analysis' 경로 하위에 매핑됩니다.
 * {@code @RequiredArgsConstructor} - final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisAPIController {

    private final IAIAnalysisService aiAnalysisService;
    private final AnalysisPreprocessorService preprocessorService;

    /**
     * Spring Security 컨텍스트에서 현재 인증된 사용자의 ID를 안전하게 가져오는 헬퍼 메소드.
     * @return 현재 로그인된 사용자의 ID (일반적으로 username)
     */
    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * 특정 월의 소비 데이터에 대한 AI 분석을 요청(생성)하는 API.
     * @param yearMonth 분석할 연월 (URL 경로 변수, 예: "2025-08")
     * @return 생성된 분석 결과 문자열을 포함하는 ResponseEntity
     */
    @PostMapping("/{yearMonth}")
    public ResponseEntity<String> analyzeSpending(@PathVariable String yearMonth) {
        log.info("▶▶▶ [API Start] analyzeSpending | yearMonth: {}", yearMonth);
        try {
            String userId = getCurrentUserId();
            log.info("AI 분석 요청 | userId: {}, month: {}", userId, yearMonth);
            String result = aiAnalysisService.analyze(userId, yearMonth);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("AI 분석 처리 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("분석 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            log.info("◀◀◀ [API End] analyzeSpending");
        }
    }

    /**
     * 특정 월의 원본 소비 데이터를 카테고리별 합계로 조회하는 API.
     * @param yearMonth 조회할 연월 (URL 경로 변수)
     * @return 카테고리별 소비 합계 데이터를 포함하는 ResponseEntity
     */
    @GetMapping("/{yearMonth}/raw-spending")
    public ResponseEntity<?> getRawSpendingByMonth(@PathVariable String yearMonth) {
        log.info("▶▶▶ [API Start] getRawSpendingByMonth | yearMonth: {}", yearMonth);
        try {
            String userId = getCurrentUserId();
            Map<String, Object> analysisInput = preprocessorService.generateAnalysisInput(userId, YearMonth.parse(yearMonth));
            Object spendingByCategory = analysisInput.get("spending_by_category");

            if (spendingByCategory == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 월의 소비 내역이 없습니다.");
            }
            return ResponseEntity.ok(spendingByCategory);
        } catch (Exception e) {
            log.error("원본 소비 데이터 조회 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("소비 내역 조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getRawSpendingByMonth");
        }
    }

    /**
     * 특정 월의 최신 분석 내역을 조회하는 API.
     * @param yearMonth 조회할 연월 (URL 경로 변수)
     * @return 해당 월의 분석 결과를 포함하는 ResponseEntity
     */
    @GetMapping("/{yearMonth}")
    public ResponseEntity<?> getAnalysisByMonth(@PathVariable String yearMonth) {
        log.info("▶▶▶ [API Start] getAnalysisByMonth | yearMonth: {}", yearMonth);
        try {
            String userId = getCurrentUserId();
            var analysis = aiAnalysisService.getAnalysisByMonth(userId, yearMonth);

            if (analysis == null) {
                log.warn("조회된 분석 내역 없음 | userId: {}, month: {}", userId, yearMonth);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 월의 분석 내역이 없습니다.");
            }
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("분석 내역 조회 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("분석 내역 조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getAnalysisByMonth");
        }
    }

    /**
     * 특정 월의 분석 내역을 삭제하는 API.
     * @param yearMonth 삭제할 연월 (URL 경로 변수)
     * @return 성공 시 204 No Content, 실패 시 에러 메시지를 포함하는 ResponseEntity
     */
    @DeleteMapping("/{yearMonth}")
    public ResponseEntity<?> deleteAnalysisByMonth(@PathVariable String yearMonth) {
        log.info("▶▶▶ [API Start] deleteAnalysisByMonth | yearMonth: {}", yearMonth);
        try {
            String userId = getCurrentUserId();
            aiAnalysisService.deleteAnalysisByMonth(userId, yearMonth);
            log.info("분석 내역 삭제 성공 | userId: {}, month: {}", userId, yearMonth);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("분석 내역 삭제 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("분석 내역 삭제에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] deleteAnalysisByMonth");
        }
    }

    /**
     * 사용자의 가장 최근 분석 결과를 조회하는 API.
     * @return 최신 분석 결과를 포함하는 ResponseEntity
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestAnalysis() {
        log.info("▶▶▶ [API Start] getLatestAnalysis");
        try {
            String userId = getCurrentUserId();
            var latest = aiAnalysisService.getLatestAnalysis(userId);
            if (latest == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("분석 결과가 없습니다.");
            }
            return ResponseEntity.ok(latest);
        } catch (Exception e) {
            log.error("최신 분석 조회 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("최신 분석 조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getLatestAnalysis");
        }
    }

    /**
     * 다음 달 소비 금액을 예측하는 API.
     * @return 소비 예측 DTO({@link PredictionDTO})를 포함하는 ResponseEntity
     */
    @GetMapping("/prediction")
    public ResponseEntity<PredictionDTO> getPrediction() {
        log.info("▶▶▶ [API Start] getPrediction");
        try {
            String userId = getCurrentUserId();
            PredictionDTO prediction = aiAnalysisService.predictNextMonthSpending(userId);
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            log.error("소비 예측 생성 중 에러 발생", e);
            return ResponseEntity.internalServerError().body(null);
        } finally {
            log.info("◀◀◀ [API End] getPrediction");
        }
    }

    /**
     * 특정 월의 분석 히스토리를 조회하는 API.
     * @param yearMonth 조회할 연월 (URL 쿼리 파라미터)
     * @return 분석 히스토리 목록을 포함하는 ResponseEntity
     */
    @GetMapping("/history")
    public ResponseEntity<?> getAnalysisHistory(@RequestParam String yearMonth) {
        log.info("▶▶▶ [API Start] getAnalysisHistory | yearMonth: {}", yearMonth);
        try {
            String userId = getCurrentUserId();
            var history = aiAnalysisService.getAnalysisHistory(userId, yearMonth);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("분석 히스토리 조회 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("히스토리 조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getAnalysisHistory");
        }
    }

    /**
     * 고유 ID를 사용하여 특정 분석 결과를 조회하는 API.
     * @param analysisId 조회할 분석의 ID (URL 경로 변수)
     * @return 특정 분석 결과를 포함하는 ResponseEntity
     */
    @GetMapping("/id/{analysisId}")
    public ResponseEntity<?> getAnalysisById(@PathVariable String analysisId) {
        log.info("▶▶▶ [API Start] getAnalysisById | analysisId: {}", analysisId);
        try {
            String userId = getCurrentUserId();
            var analysis = aiAnalysisService.getAnalysisById(userId, analysisId);
            if (analysis == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 ID의 분석 결과가 없습니다.");
            }
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("ID 기반 분석 결과 조회 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("분석 결과 조회에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] getAnalysisById");
        }
    }

    /**
     * 두 개의 분석 결과를 비교하는 API.
     * @param analysisId1 비교할 첫 번째 분석 ID (URL 쿼리 파라미터)
     * @param analysisId2 비교할 두 번째 분석 ID (URL 쿼리 파라미터)
     * @return 두 분석의 비교 결과를 포함하는 ResponseEntity
     */
    @GetMapping("/compare")
    public ResponseEntity<?> compareAnalysis(@RequestParam String analysisId1, @RequestParam String analysisId2) {
        log.info("▶▶▶ [API Start] compareAnalysis | analysisId1: {}, analysisId2: {}", analysisId1, analysisId2);
        try {
            String userId = getCurrentUserId();
            var comparison = aiAnalysisService.compareAnalysis(userId, analysisId1, analysisId2);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            log.error("분석 비교 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("분석 비교에 실패했습니다.");
        } finally {
            log.info("◀◀◀ [API End] compareAnalysis");
        }
    }
}
