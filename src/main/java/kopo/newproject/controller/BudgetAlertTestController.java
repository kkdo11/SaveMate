package kopo.newproject.controller;

import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.repository.jpa.UserInfoRepository;
import kopo.newproject.repository.mongo.SpendingRepository;
import kopo.newproject.service.IReportService;
import kopo.newproject.service.impl.BudgetAlertService;
import kopo.newproject.service.impl.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class BudgetAlertTestController {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertTestController.class);

    private final BudgetAlertService budgetAlertService;
    private final UserInfoRepository userInfoRepository;
    private final SpendingRepository spendingRepository;
    private final PasswordEncoder passwordEncoder;
    private final BudgetService budgetService; // BudgetService 주입
    private final IReportService reportService; // IReportService 주입

    @GetMapping("/send-report")
    public ResponseEntity<String> sendTestReport(@RequestParam String userId) {
        try {
            reportService.generateAndSendReportForUser(userId, YearMonth.now());
            return ResponseEntity.ok(userId + "님에게 테스트 리포트 발송을 성공했습니다.");
        } catch (Exception e) {
            log.error("테스트 리포트 발송 중 오류", e);
            return ResponseEntity.internalServerError().body(userId + "님에게 리포트 발송 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/budget-alert")
    public ResponseEntity<String> triggerBudgetAlert() {
        try {
            budgetAlertService.checkBudgetAndSendAlerts();
            return ResponseEntity.ok("예산 초과 예측 알림 로직을 성공적으로 실행했습니다. 서버 로그와 메일함을 확인하세요.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("알림 로직 실행 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/adjust-budgets")
    public ResponseEntity<String> triggerBudgetAdjustment() {
        try {
            budgetService.adjustBudgetsBasedOnInflation();
            return ResponseEntity.ok("물가 반영 예산 자동 조정 로직을 성공적으로 실행했습니다. 서버 로그를 확인하세요.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("예산 조정 로직 실행 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/create-sample-users")
    public ResponseEntity<String> createSampleUsers() {
        log.info("[TestController] 샘플 사용자 생성 요청됨");
        try {
            // 기존 사용자 삭제 (테스트 용이성을 위해)
            userInfoRepository.deleteAll();

            // 샘플 사용자 데이터 생성
            List<UserInfoEntity> users = new ArrayList<>();
            users.add(UserInfoEntity.builder()
                    .userId("user1")
                    .email("user1@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("테스트유저1")
                    .gender("M")
                    .birthDate("1995-05-10")
                    .globalAlertEnabled(true)
                    .build());
            users.add(UserInfoEntity.builder()
                    .userId("user2")
                    .email("user2@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("테스트유저2")
                    .gender("F")
                    .birthDate("1998-08-20")
                    .globalAlertEnabled(true)
                    .build());
            users.add(UserInfoEntity.builder()
                    .userId("user3")
                    .email("user3@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("테스트유저3")
                    .gender("M")
                    .birthDate("1980-01-01")
                    .globalAlertEnabled(true)
                    .build());
            users.add(UserInfoEntity.builder()
                    .userId("user4")
                    .email("user4@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .name("테스트유저4")
                    .gender("F")
                    .birthDate("1975-11-15")
                    .globalAlertEnabled(true)
                    .build());

            userInfoRepository.saveAll(users);
            log.info("[TestController] 샘플 사용자 {}명 생성 완료.", users.size());
            return ResponseEntity.ok("샘플 사용자 생성 완료.");
        } catch (Exception e) {
            log.error("[TestController] 샘플 사용자 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("샘플 사용자 생성 실패: " + e.getMessage());
        }
    }

    @GetMapping("/create-sample-spendings")
    public ResponseEntity<String> createSampleSpendings() {
        log.info("[TestController] 샘플 지출 내역 생성 요청됨");
        try {
            // 기존 지출 내역 삭제 (테스트 용이성을 위해)
            spendingRepository.deleteAll();

            List<SpendingEntity> spendings = new ArrayList<>();
            YearMonth currentMonth = YearMonth.now();

            // user1 (M, 20대) 지출
            spendings.add(SpendingEntity.builder().userId("user1").date(LocalDate.now()).category("식비").amount(new BigDecimal("50000")).description("점심").build());
            spendings.add(SpendingEntity.builder().userId("user1").date(LocalDate.now()).category("교통/차량").amount(new BigDecimal("20000")).description("주유").build());
            spendings.add(SpendingEntity.builder().userId("user1").date(LocalDate.now()).category("문화생활").amount(new BigDecimal("30000")).description("영화").build());

            // user2 (F, 20대) 지출
            spendings.add(SpendingEntity.builder().userId("user2").date(LocalDate.now()).category("식비").amount(new BigDecimal("60000")).description("저녁").build());
            spendings.add(SpendingEntity.builder().userId("user2").date(LocalDate.now()).category("외식").amount(new BigDecimal("40000")).description("카페").build());
            spendings.add(SpendingEntity.builder().userId("user2").date(LocalDate.now()).category("교통/차량").amount(new BigDecimal("15000")).description("대중교통").build());

            // user3 (M, 40대) 지출
            spendings.add(SpendingEntity.builder().userId("user3").date(LocalDate.now()).category("식비").amount(new BigDecimal("80000")).description("마트").build());
            spendings.add(SpendingEntity.builder().userId("user3").date(LocalDate.now()).category("주거").amount(new BigDecimal("100000")).description("월세").build());

            // user4 (F, 40대) 지출
            spendings.add(SpendingEntity.builder().userId("user4").date(LocalDate.now()).category("식비").amount(new BigDecimal("70000")).description("배달").build());
            spendings.add(SpendingEntity.builder().userId("user4").date(LocalDate.now()).category("교육").amount(new BigDecimal("120000")).description("학원비").build());

            spendingRepository.saveAll(spendings);
            log.info("[TestController] 샘플 지출 내역 {}건 생성 완료.", spendings.size());
            return ResponseEntity.ok("샘플 지출 내역 생성 완료.");
        } catch (Exception e) {
            log.error("[TestController] 샘플 지출 내역 생성 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("샘플 지출 내역 생성 실패: " + e.getMessage());
        }
    }
}
