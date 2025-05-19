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
}
