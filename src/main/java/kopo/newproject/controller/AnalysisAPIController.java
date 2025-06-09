package kopo.newproject.controller;

import kopo.newproject.service.IAIAnalysisService;
import kopo.newproject.service.impl.AnalysisPreprocessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisAPIController {

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

    // 월별 분석 내역 조회 (복수 리턴 대비 List로)
    @GetMapping("/{yearMonth}")
    public ResponseEntity<?> getAnalysisByMonth(@PathVariable String yearMonth) {
        try {
            String userId = getCurrentUserId();
            var analyses = aiAnalysisService.getAnalysisByMonth(userId, yearMonth);
            return ResponseEntity.ok(analyses);
        } catch (Exception e) {
            log.error("❌ 분석 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("❌ 조회 실패: " + e.getMessage());
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
