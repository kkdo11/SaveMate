package kopo.newproject.controller;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.repository.jpa.UserInfoRepository;
import kopo.newproject.repository.mongo.SpendingRepository;
import kopo.newproject.service.IReportService;
import kopo.newproject.service.impl.BudgetAlertService;
import kopo.newproject.service.impl.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * ❗️❗️❗️ 테스트 및 디버깅 전용 컨트롤러 ❗️❗️❗️
 * <p>
 * 이 컨트롤러는 개발 환경에서만 사용되어야 하며, 실제 운영(Production) 환경에서는 반드시 비활성화하거나 제거해야 합니다.
 * 데이터베이스의 데이터를 삭제하거나, 스케줄링된 작업을 수동으로 실행하는 등 민감한 작업을 수행하는 API를 포함하고 있습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class BudgetAlertTestController {

    private final BudgetAlertService budgetAlertService;
    private final UserInfoRepository userInfoRepository;
    private final SpendingRepository spendingRepository;
    private final PasswordEncoder passwordEncoder;
    private final BudgetService budgetService;
    private final IReportService reportService;

    /**
     * 특정 사용자에 대한 월간 리포트 생성을 수동으로 실행합니다.
     * @param userId 리포트를 생성할 사용자 ID
     * @return 작업 결과 메시지를 포함하는 ResponseEntity
     */
    @GetMapping("/send-report")
    public ResponseEntity<String> sendTestReport(@RequestParam String userId) {
        log.info("▶▶▶ [TEST API] sendTestReport | userId: {}", userId);
        try {
            reportService.generateAndSendReportForUser(userId, YearMonth.now());
            log.info("테스트 리포트 발송 성공 | userId: {}", userId);
            return ResponseEntity.ok(userId + "님에게 테스트 리포트 발송을 성공했습니다.");
        } catch (Exception e) {
            log.error("테스트 리포트 발송 중 에러 발생", e);
            return ResponseEntity.internalServerError().body(userId + "님에게 리포트 발송 중 오류 발생: " + e.getMessage());
        } finally {
            log.info("◀◀◀ [TEST API] sendTestReport");
        }
    }

    /**
     * 예산 초과 예측 알림 스케줄러를 수동으로 실행합니다.
     * @return 작업 결과 메시지를 포함하는 ResponseEntity
     */
    @GetMapping("/budget-alert")
    public ResponseEntity<String> triggerBudgetAlert() {
        log.info("▶▶▶ [TEST API] triggerBudgetAlert");
        try {
            budgetAlertService.checkBudgetAndSendAlerts();
            log.info("예산 초과 예측 알림 로직 실행 완료");
            return ResponseEntity.ok("예산 초과 예측 알림 로직을 성공적으로 실행했습니다. 서버 로그와 메일함을 확인하세요.");
        } catch (Exception e) {
            log.error("예산 초과 예측 알림 로직 실행 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("알림 로직 실행 중 오류 발생: " + e.getMessage());
        } finally {
            log.info("◀◀◀ [TEST API] triggerBudgetAlert");
        }
    }

    /**
     * 물가 반영 예산 자동 조정 스케줄러를 수동으로 실행합니다.
     * @return 작업 결과 메시지를 포함하는 ResponseEntity
     */
    @GetMapping("/adjust-budgets")
    public ResponseEntity<String> triggerBudgetAdjustment() {
        log.info("▶▶▶ [TEST API] triggerBudgetAdjustment");
        try {
            budgetService.adjustBudgetsBasedOnInflation();
            log.info("물가 반영 예산 자동 조정 로직 실행 완료");
            return ResponseEntity.ok("물가 반영 예산 자동 조정 로직을 성공적으로 실행했습니다. 서버 로그를 확인하세요.");
        } catch (Exception e) {
            log.error("예산 자동 조정 로직 실행 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("예산 조정 로직 실행 중 오류 발생: " + e.getMessage());
        } finally {
            log.info("◀◀◀ [TEST API] triggerBudgetAdjustment");
        }
    }

    /**
     * ❗️(주의)❗️ 모든 사용자를 삭제하고, 테스트용 샘플 사용자들을 생성합니다.
     * @return 작업 결과 메시지를 포함하는 ResponseEntity
     */
    @GetMapping("/create-sample-users")
    public ResponseEntity<String> createSampleUsers() {
        log.info("▶▶▶ [TEST API] createSampleUsers");
        try {
            log.warn("기존의 모든 사용자 정보를 삭제합니다.");
            userInfoRepository.deleteAll();

            List<UserInfoEntity> users = new ArrayList<>();
            users.add(UserInfoEntity.builder().userId("user1").email("user1@example.com").password(passwordEncoder.encode("password123")).name("테스트유저1").gender("M").birthDate("1995-05-10").globalAlertEnabled(true).build());
            users.add(UserInfoEntity.builder().userId("user2").email("user2@example.com").password(passwordEncoder.encode("password123")).name("테스트유저2").gender("F").birthDate("1998-08-20").globalAlertEnabled(true).build());
            users.add(UserInfoEntity.builder().userId("user3").email("user3@example.com").password(passwordEncoder.encode("password123")).name("테스트유저3").gender("M").birthDate("1980-01-01").globalAlertEnabled(true).build());
            users.add(UserInfoEntity.builder().userId("user4").email("user4@example.com").password(passwordEncoder.encode("password123")).name("테스트유저4").gender("F").birthDate("1975-11-15").globalAlertEnabled(true).build());

            userInfoRepository.saveAll(users);
            log.info("샘플 사용자 {}명 생성 완료.", users.size());
            return ResponseEntity.ok("샘플 사용자 생성 완료.");
        } catch (Exception e) {
            log.error("샘플 사용자 생성 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("샘플 사용자 생성 실패: " + e.getMessage());
        } finally {
            log.info("◀◀◀ [TEST API] createSampleUsers");
        }
    }

    /**
     * ❗️(주의)❗️ 모든 지출 내역을 삭제하고, 테스트용 샘플 지출 내역을 생성합니다.
     * @return 작업 결과 메시지를 포함하는 ResponseEntity
     */
    @GetMapping("/create-sample-spendings")
    public ResponseEntity<String> createSampleSpendings() {
        log.info("▶▶▶ [TEST API] createSampleSpendings");
        try {
            log.warn("기존의 모든 지출 내역을 삭제합니다.");
            spendingRepository.deleteAll();

            List<SpendingEntity> spendings = new ArrayList<>();
            // ... (샘플 데이터 생성 로직)
            spendings.add(SpendingEntity.builder().userId("user1").date(LocalDate.now()).category("식비").amount(new BigDecimal("50000")).description("점심").build());
            spendings.add(SpendingEntity.builder().userId("user1").date(LocalDate.now()).category("교통/차량").amount(new BigDecimal("20000")).description("주유").build());
            spendings.add(SpendingEntity.builder().userId("user1").date(LocalDate.now()).category("문화생활").amount(new BigDecimal("30000")).description("영화").build());
            spendings.add(SpendingEntity.builder().userId("user2").date(LocalDate.now()).category("식비").amount(new BigDecimal("60000")).description("저녁").build());
            spendings.add(SpendingEntity.builder().userId("user2").date(LocalDate.now()).category("외식").amount(new BigDecimal("40000")).description("카페").build());
            spendings.add(SpendingEntity.builder().userId("user2").date(LocalDate.now()).category("교통/차량").amount(new BigDecimal("15000")).description("대중교통").build());
            spendings.add(SpendingEntity.builder().userId("user3").date(LocalDate.now()).category("식비").amount(new BigDecimal("80000")).description("마트").build());
            spendings.add(SpendingEntity.builder().userId("user3").date(LocalDate.now()).category("주거").amount(new BigDecimal("100000")).description("월세").build());
            spendings.add(SpendingEntity.builder().userId("user4").date(LocalDate.now()).category("식비").amount(new BigDecimal("70000")).description("배달").build());
            spendings.add(SpendingEntity.builder().userId("user4").date(LocalDate.now()).category("교육").amount(new BigDecimal("120000")).description("학원비").build());

            spendingRepository.saveAll(spendings);
            log.info("샘플 지출 내역 {}건 생성 완료.", spendings.size());
            return ResponseEntity.ok("샘플 지출 내역 생성 완료.");
        } catch (Exception e) {
            log.error("샘플 지출 내역 생성 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("샘플 지출 내역 생성 실패: " + e.getMessage());
        } finally {
            log.info("◀◀◀ [TEST API] createSampleSpendings");
        }
    }
}
