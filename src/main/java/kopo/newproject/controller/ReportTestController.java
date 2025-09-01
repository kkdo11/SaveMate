package kopo.newproject.controller;

import kopo.newproject.service.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class ReportTestController {

    private final IReportService reportService;

    @GetMapping("/send-report/{userId}")
    public String sendTestReport(@PathVariable String userId) {
        log.info("Test report sending started for user: {}", userId);
        try {
            // 지난달을 기준으로 리포트를 생성하고 발송합니다.
            reportService.generateAndSendReportForUser(userId, YearMonth.now().minusMonths(1));
            return String.format("'%s'님에게 테스트 리포트 발송 성공!", userId);
        } catch (Exception e) {
            log.error("Failed to send test report for user: {}", userId, e);
            return String.format("리포트 발송 실패: %s", e.getMessage());
        }
    }
}
