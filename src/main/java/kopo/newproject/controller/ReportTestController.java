package kopo.newproject.controller;

import kopo.newproject.service.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

/**
 * ❗️❗️❗️ 테스트 및 디버깅 전용 컨트롤러 ❗️❗️❗️
 * <p>
 * 월간 리포트 생성 및 발송 기능의 수동 실행을 위해 사용됩니다.
 * 운영(Production) 환경에서는 반드시 비활성화하거나 제거해야 합니다.
 */
@Slf4j
@RestController
@RequestMapping("/test") // 다른 테스트용 컨트롤러와 경로를 맞추는 것을 고려해볼 수 있습니다. (예: /api/v1/test)
@RequiredArgsConstructor
public class ReportTestController {

    private final IReportService reportService;

    /**
     * 특정 사용자에 대한 월간 리포트 생성을 수동으로 실행(trigger)하는 테스트 API.
     *
     * @param userId 리포트를 생성하고 발송할 대상 사용자의 ID
     * @return 작업 성공 또는 실패 메시지를 담은 {@link ResponseEntity}
     */
    @GetMapping("/send-report/{userId}")
    public ResponseEntity<String> sendTestReport(@PathVariable String userId) {
        log.info("▶▶▶ [TEST API] sendTestReport | userId: {}", userId);
        try {
            // 지난달을 기준으로 리포트를 생성하고 발송합니다.
            reportService.generateAndSendReportForUser(userId, YearMonth.now().minusMonths(1));
            String successMsg = String.format("'%s'님에게 테스트 리포트 발송을 성공했습니다!", userId);
            log.info(successMsg);
            return ResponseEntity.ok(successMsg);
        } catch (Exception e) {
            String errorMsg = String.format("리포트 발송 실패: %s", e.getMessage());
            log.error("테스트 리포트 발송 중 에러 발생 | userId: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMsg);
        } finally {
            log.info("◀◀◀ [TEST API] sendTestReport");
        }
    }
}
