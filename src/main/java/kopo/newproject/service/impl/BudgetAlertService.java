package kopo.newproject.service.impl;

import kopo.newproject.dto.MailDTO;
import kopo.newproject.dto.SpendingTotalDTO;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.repository.entity.jpa.BudgetAlertLogEntity;
import kopo.newproject.repository.jpa.BudgetRepository;
import kopo.newproject.repository.jpa.BudgetAlertLogRepository;
import kopo.newproject.repository.mongo.SpendingRepository;
import kopo.newproject.service.IMailService;
import kopo.newproject.service.ISpendingService;
import kopo.newproject.service.IUserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetAlertService {

    private static final Logger log = LoggerFactory.getLogger(BudgetAlertService.class);

    private final BudgetRepository budgetRepository;
    private final SpendingRepository spendingRepository;
    private final BudgetAlertLogRepository budgetAlertLogRepository;
    private final IMailService mailService;
    private final IUserInfoService userInfoService; // 사용자 정보 서비스 추가
    private final ISpendingService spendingService; // 지출 서비스 추가

    // 매일 새벽 4시에 실행
    @Scheduled(cron = "0 15 00  * * ?")
    public void checkBudgetAndSendAlerts() {
        log.info("예산 초과 예측 알림 스케줄러 시작");

        LocalDate today = LocalDate.now();
        YearMonth currentYearMonth = YearMonth.from(today);
        int year = currentYearMonth.getYear();
        int month = currentYearMonth.getMonthValue();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        int dayOfMonth = today.getDayOfMonth();

        // 모든 예산 정보를 가져옴
        List<BudgetEntity> budgets = budgetRepository.findAll();

        for (BudgetEntity budget : budgets) {
            // 이번 달 예산만 대상으로 함
            if (budget.getYear() != year || budget.getMonth() != month) {
                continue;
            }

            String userId = budget.getUserId();
            String category = budget.getCategory();

            // ✅ 사용자 전역 알림 설정 확인
            UserInfoDTO user = null;
            try {
                user = userInfoService.findByUserId(userId);
            } catch (Exception e) {
                log.error("사용자 정보 조회 중 오류 발생: {}", userId, e);
                continue;
            }

            if (user == null || user.globalAlertEnabled() == null || !user.globalAlertEnabled()) {
                log.info("사용자 {}의 전역 알림이 비활성화되어 건너뜀.", userId);
                continue;
            }

            // 이미 이번 달에 알림을 보냈는지 확인
            boolean alreadySent = budgetAlertLogRepository.existsByUserIdAndYearAndMonthAndCategory(userId, year, month, category);
            if (alreadySent) {
                log.info("사용자 {}의 {} 카테고리에 대한 알림이 이미 발송되어 건너뜜.", userId, category);
                continue;
            }

            // 현재까지의 지출액 계산
            BigDecimal currentSpending;
            try {
                currentSpending = spendingService.calculateMonthlySpendingSum(userId, year, month, category);
            } catch (Exception e) {
                log.error("사용자 {}의 {} 카테고리 지출 합계 계산 중 오류 발생. 건너뜁니다.", userId, category, e);
                continue;
            }

            // 디버깅 로그 추가
            log.info("\n[예산 검사] 사용자: {}, 카테고리: {}, 총 예산: {}\n" +
                     " -> 현재 지출액: {}\n",
                     userId, category, budget.getTotalBudget(), currentSpending);

            // 지출이 0이면 계산할 필요 없음
            if (currentSpending.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // 하루 평균 지출액 계산

            // 하루 평균 지출액 계산
            BigDecimal dailyAverageSpending = currentSpending.divide(BigDecimal.valueOf(dayOfMonth), 2, RoundingMode.HALF_UP);

            // 남은 날짜 계산
            int remainingDays = daysInMonth - dayOfMonth;

            // 예상 총 지출액 계산
            BigDecimal estimatedTotalSpending = currentSpending.add(dailyAverageSpending.multiply(BigDecimal.valueOf(remainingDays)));

            // 디버깅 로그 추가
            log.info(" -> 하루 평균: {}, 남은 날짜: {}, 최종 예상 지출액: {}\n",
                    dailyAverageSpending, remainingDays, estimatedTotalSpending);

            // 예상 지출액이 예산을 초과하는 경우
            if (estimatedTotalSpending.compareTo(budget.getTotalBudget()) > 0) {
                log.info("사용자 {}의 {} 카테고리 예산 초과 예측됨. 알림 발송 시도.", userId, category);
                sendAlert(userId, budget, estimatedTotalSpending);
            }
        }
        log.info("예산 초과 예측 알림 스케줄러 종료");
    }

    private void sendAlert(String userId, BudgetEntity budget, BigDecimal estimatedSpending) {
        try {
            // 사용자 정보에서 이메일을 가져오는 로직
            UserInfoDTO user = userInfoService.findByUserId(userId);
            if (user == null || user.email() == null) {
                log.warn("사용자 {}의 이메일 정보를 찾을 수 없어 알림을 발송할 수 없습니다.", userId);
                return;
            }
            String userEmail = user.email();

            String subject = String.format("[SaveMate] %d월 %s 카테고리 예산 초과 예측 알림", budget.getMonth(), budget.getCategory());
            String content = String.format(
                "안녕하세요, %s님.\n" +
                "이번 달 %s 카테고리의 소비 속도를 분석한 결과, 이달 말까지 약 %,.0f원을 사용하실 것으로 예측됩니다.\n" +
                "설정하신 예산 %,.0f원을 초과할 가능성이 높으니, 남은 기간 동안 지출에 유의해 주세요.",
                user.name(), // userId 대신 사용자 이름 사용
                budget.getCategory(),
                estimatedSpending,
                budget.getTotalBudget()
            );

            // MailDTO를 사용하여 메일 발송
            MailDTO mailDTO = MailDTO.builder()
                    .toMail(userEmail)
                    .title(subject)
                    .contents(content)
                    .build();
            mailService.doSendMail(mailDTO);

            // 알림 발송 기록 저장
            BudgetAlertLogEntity logEntity = BudgetAlertLogEntity.builder()
                    .userId(userId)
                    .year(budget.getYear())
                    .month(budget.getMonth())
                    .category(budget.getCategory())
                    .sentAt(LocalDateTime.now())
                    .build();
            budgetAlertLogRepository.save(logEntity);

            log.info("사용자 {}에게 알림 메일 발송 및 로그 저장 완료", userId);

        } catch (Exception e) {
            log.error("알림 메일 발송 중 오류 발생: 사용자 - {}, 카테고리 - {}", userId, budget.getCategory(), e);
        }
    }
}