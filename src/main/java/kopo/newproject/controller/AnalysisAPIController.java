package kopo.newproject.controller;

import kopo.newproject.dto.PredictionDTO;
import kopo.newproject.service.IAIAnalysisService;
import kopo.newproject.service.impl.AnalysisPreprocessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisAPIController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisAPIController.class);

    private final IAIAnalysisService aiAnalysisService;
    private final AnalysisPreprocessorService preprocessorService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/{yearMonth}")
    public ResponseEntity<String> analyzeSpending(@PathVariable String yearMonth) {
        try {
            String userId = getCurrentUserId(); // 서버에서 사용자 판별
            log.info("📥 [AI 분석 요청] userId={}, month={}", userId, yearMonth);
            String result = aiAnalysisService.analyze(userId, yearMonth);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 분석 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("❌ 분석 실패: " + e.getMessage());
        }
    }

    // 월별 원본 소비 데이터 조회 (카테고리별 합계)
    @GetMapping("/{yearMonth}/raw-spending")
    public ResponseEntity<?> getRawSpendingByMonth(@PathVariable String yearMonth) {
        try {
            String userId = getCurrentUserId();
            log.info("INFO: Get raw spending for userId={}, month={}", userId, yearMonth);

            // 전처리 서비스를 사용하여 데이터 생성
            Map<String, Object> analysisInput = preprocessorService.generateAnalysisInput(userId, YearMonth.parse(yearMonth));

            // 필요한 데이터만 추출하여 반환
            Object spendingByCategory = analysisInput.get("spending_by_category");

            if (spendingByCategory == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 월의 소비 내역이 없습니다.");
            }

            return ResponseEntity.ok(spendingByCategory);

        } catch (Exception e) {
            log.error("ERROR: Failed to get raw spending for month {}: {}", yearMonth, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("소비 내역 조회에 실패했습니다.");
        }
    }

    // 월별 최신 분석 내역 조회
    @GetMapping("/{yearMonth}")
    public ResponseEntity<?> getAnalysisByMonth(@PathVariable String yearMonth) {
        try {
            String userId = getCurrentUserId();
            log.info("INFO: Get analysis for userId={}, month={}", userId, yearMonth);
            var analysis = aiAnalysisService.getAnalysisByMonth(userId, yearMonth);

            if (analysis == null) {
                log.warn("WARN: No analysis found for userId={}, month={}", userId, yearMonth);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 월의 분석 내역이 없습니다.");
            }

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            log.error("ERROR: Failed to get analysis for month {}: {}", yearMonth, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("분석 내역 조회에 실패했습니다.");
        }
    }

    // 월별 분석 내역 삭제
    @DeleteMapping("/{yearMonth}")
    public ResponseEntity<?> deleteAnalysisByMonth(@PathVariable String yearMonth) {
        try {
            String userId = getCurrentUserId();
            aiAnalysisService.deleteAnalysisByMonth(userId, yearMonth);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("❌ 분석 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("❌ 삭제 실패: " + e.getMessage());
        }
    }

    // 최신 분석 결과 조회
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestAnalysis() {
        try {
            String userId = getCurrentUserId();
            var latest = aiAnalysisService.getLatestAnalysis(userId);
            if (latest == null) {
                return ResponseEntity.status(404).body("분석 결과 없음");
            }
            return ResponseEntity.ok(latest);
        } catch (Exception e) {
            log.error("❌ 최신 분석 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("❌ 조회 실패: " + e.getMessage());
        }
    }

    @GetMapping("/prediction")
    public ResponseEntity<PredictionDTO> getPrediction() {
        try {
            String userId = getCurrentUserId();
            log.info("INFO: Get prediction for userId={}", userId);
            PredictionDTO prediction = aiAnalysisService.predictNextMonthSpending(userId);
            return ResponseEntity.ok(prediction);
        } catch (Exception e) {
            log.error("❌ 예측 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // 분석 히스토리 조회
    @GetMapping("/history")
    public ResponseEntity<?> getAnalysisHistory(@RequestParam String yearMonth) {
        try {
            String userId = getCurrentUserId();
            var history = aiAnalysisService.getAnalysisHistory(userId, yearMonth);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("❌ 히스토리 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("❌ 히스토리 조회 실패: " + e.getMessage());
        }
    }

    // 특정 분석 결과 조회
    @GetMapping("/id/{analysisId}")
    public ResponseEntity<?> getAnalysisById(@PathVariable String analysisId) {
        try {
            String userId = getCurrentUserId();
            var analysis = aiAnalysisService.getAnalysisById(userId, analysisId);
            if (analysis == null) {
                return ResponseEntity.status(404).body("분석 결과 없음");
            }
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            log.error("❌ 분석 결과 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("❌ 조회 실패: " + e.getMessage());
        }
    }

    // 분석 결과 비교
    @GetMapping("/compare")
    public ResponseEntity<?> compareAnalysis(@RequestParam String analysisId1, @RequestParam String analysisId2) {
        try {
            String userId = getCurrentUserId();
            var comparison = aiAnalysisService.compareAnalysis(userId, analysisId1, analysisId2);
            return ResponseEntity.ok(comparison);
        } catch (Exception e) {
            log.error("❌ 분석 비교 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("❌ 비교 실패: " + e.getMessage());
        }
    }
}
