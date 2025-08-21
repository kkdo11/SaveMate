package kopo.newproject.service.impl;

import kopo.newproject.dto.BudgetDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.jpa.BudgetRepository;
import kopo.newproject.repository.jpa.UserInfoRepository;
import kopo.newproject.service.IBokService;
import kopo.newproject.service.IBudgetService;
import kopo.newproject.service.IMailService; // IMailService 임포트 추가
import kopo.newproject.dto.MailDTO; // MailDTO 임포트 추가
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("BudgetService")

public class BudgetService implements IBudgetService {

    private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

    private final BudgetRepository budgetRepository;
    private final SpendingService spendingService;
    private final IBokService bokService;
    private final UserInfoRepository userInfoRepository;
    private final IMailService mailService; // IMailService 주입

    public BudgetService(BudgetRepository budgetRepository, SpendingService spendingService, IBokService bokService, UserInfoRepository userInfoRepository, IMailService mailService) {
        this.budgetRepository = budgetRepository;
        this.spendingService = spendingService;
        this.bokService = bokService;
        this.userInfoRepository = userInfoRepository;
        this.mailService = mailService;
    }
    
    

    // 예산 생성
    @Override
    public BudgetEntity createBudget(String userId, BudgetDTO dto) {
        BigDecimal used = spendingService.calculateMonthlySpendingSum(userId, dto.getYear(), dto.getMonth(), dto.getCategory());

        BudgetEntity entity = BudgetEntity.builder()
                .userId(userId)
                .year(dto.getYear())
                .month(dto.getMonth())
                .category(dto.getCategory())
                .totalBudget(dto.getTotalBudget() != null ? dto.getTotalBudget() : BigDecimal.ZERO)
                .build();

        return budgetRepository.save(entity);
    }


    @Override
    @Transactional
    public boolean updateBudget(String userId, Long budgetId, BudgetDTO dto) {
        Optional<BudgetEntity> optional = budgetRepository.findById(budgetId);

        if (optional.isPresent() && optional.get().getUserId().equals(userId)) {
            BudgetEntity entity = optional.get();
            entity.updateBudgetInfo(dto.getYear(), dto.getMonth(), dto.getCategory(), dto.getTotalBudget());
            budgetRepository.save(entity);
            return true;
        }

        return false;
    }


    // 예산 삭제
    @Override
    @Transactional
    public boolean deleteBudget(String userId, Long budgetId) {
        Optional<BudgetEntity> optional = budgetRepository.findById(budgetId);

        if (optional.isPresent() && optional.get().getUserId().equals(userId)) {
            budgetRepository.deleteById(budgetId);
            return true;
        }

        return false;
    }

    // 특정 연/월 예산 조회 (단일) -> 사용처가 있는지 확인 필요
    @Override
    public BudgetEntity getBudgetByUserIdAndYearMonth(String userId, int year, int month) {
        Optional<BudgetEntity> optional = budgetRepository.findByUserIdAndYearAndMonth(userId, year, month).stream().findFirst();
        if (optional.isEmpty()) return null;

        BudgetEntity entity = optional.get();
        BigDecimal used = spendingService.calculateMonthlySpendingSum(userId, year, month, entity.getCategory());
        entity.setUsedBudget(used);
        return entity;
    }


    @Override
    @Transactional(readOnly = true)
    public BudgetDTO getBudgetById(String userId, Long budgetId) {
        Optional<BudgetEntity> optional = budgetRepository.findByUserIdAndBudgetId(userId, budgetId);

        if (optional.isEmpty()) return null;

        BudgetEntity entity = optional.get();

        return BudgetDTO.builder()
                .budgetId(entity.getBudgetId())
                .userId(entity.getUserId())
                .year(entity.getYear())
                .month(entity.getMonth())
                .category(entity.getCategory())
                .totalBudget(entity.getTotalBudget())
                .usedBudget(entity.getUsedBudget())
                .remainingBudget(entity.getRemainingBudget())
                .build();
    }


    // 유저별 전체 예산 목록 조회
    @Override
    public List<BudgetEntity> getBudgetsByUserId(String userId) {
        List<BudgetEntity> budgets = budgetRepository.findAllByUserId(userId);
        for (BudgetEntity b : budgets) {
            BigDecimal used = spendingService.calculateMonthlySpendingSum(
                    userId, b.getYear(), b.getMonth(), b.getCategory()
            );
            b.setUsedBudget(used);
        }
        return budgets;
    }


    // 특정 연/월 모든 예산(카테고리별) 조회
    @Override
    public List<BudgetEntity> getBudgetsByUserIdAndYearMonth(String userId, int year, int month) {
        List<BudgetEntity> budgets = budgetRepository.findAllByUserIdAndYearAndMonth(userId, year, month);
        for (BudgetEntity b : budgets) {
            BigDecimal used = spendingService.calculateMonthlySpendingSum(userId, year, month, b.getCategory());
            b.setUsedBudget(used);
        }
        return budgets;
    }




    @Override
    public List<BudgetEntity> findAll() {
        return budgetRepository.findAll();
    }

    @Override
    public List<BudgetEntity> findByMonthAndCategory(Integer month, String category) {
        if (month != null && category != null) {
            return budgetRepository.findByMonthAndCategory(month, category);
        } else if (month != null) {
            return budgetRepository.findByMonth(month);
        } else if (category != null) {
            return budgetRepository.findByCategory(category);
        } else {
            return budgetRepository.findAll();
        }
    }

    @Override
    public Map<String, Integer> getTotalBudgetByMonth(String userId) {
        List<BudgetEntity> budgets = budgetRepository.findAllByUserId(userId);

        Map<String, Integer> result = new HashMap<>();
        for (BudgetEntity b : budgets) {
            String key = String.format("%04d-%02d", b.getYear(), b.getMonth());
            int amount = b.getTotalBudget() != null ? b.getTotalBudget().intValue() : 0;
            result.put(key, result.getOrDefault(key, 0) + amount);
        }
        return result;
    }

    @Override
    @Transactional
    public List<BudgetEntity> adjustAllBudgetsForCpi(String userId) {
        log.info("START: adjustAllBudgetsForCpi for user: {}", userId);

        // 1. 최신 물가 상승률 조회
        double growthRate = bokService.getLatestCpiGrowthRate();
        if (growthRate == 0.0) {
            log.warn("CPI growth rate is 0.0 or could not be fetched. No adjustments will be made.");
            return Collections.emptyList();
        }

        BigDecimal inflationMultiplier = BigDecimal.valueOf(1 + (growthRate / 100.0));
        log.info("Applying CPI growth rate of {}% (Multiplier: {})", growthRate, inflationMultiplier);

        // 2. 현재 월의 사용자 예산 조회
        YearMonth currentMonth = YearMonth.now();
        List<BudgetEntity> userBudgets = budgetRepository.findAllByUserIdAndYearAndMonth(
                userId, currentMonth.getYear(), currentMonth.getMonthValue()
        );

        if (userBudgets.isEmpty()) {
            log.info("User {} has no budgets for the current month ({}). No adjustments needed.", userId, currentMonth);
            return Collections.emptyList();
        }

        // 3. 예산 조정 및 저장
        for (BudgetEntity budget : userBudgets) {
            BigDecimal originalBudget = budget.getTotalBudget();
            BigDecimal adjustedBudget = originalBudget.multiply(inflationMultiplier).setScale(0, RoundingMode.HALF_UP);

            log.info("Adjusting budget for category '{}': From {} to {}", budget.getCategory(), originalBudget, adjustedBudget);

            budget.updateBudgetInfo(budget.getYear(), budget.getMonth(), budget.getCategory(), adjustedBudget);
            budgetRepository.save(budget);
        }

        log.info("SUCCESS: adjustAllBudgetsForCpi for user: {}", userId);
        return userBudgets;
    }

    /**
     * 매월 1일 자정에 실행되어 물가지수를 반영하여 예산을 자동 조정합니다.
     */
    @Scheduled(cron = "0 0 0 1 * ?") // 매월 1일 0시 0분 0초에 실행
    public void adjustBudgetsBasedOnInflation() {
        log.info("START: Scheduled job adjustBudgetsBasedOnInflation");

        // 1. 모든 사용자 조회
        List<UserInfoEntity> allUsers = userInfoRepository.findAll();
        log.info("Scheduled job: Found {} users to potentially adjust budgets for.", allUsers.size());

        // 물가 상승률은 한 번만 계산
        double growthRate = bokService.getLatestCpiGrowthRate();
        if (growthRate == 0.0) {
            log.warn("Scheduled job: CPI growth rate is 0.0 or could not be fetched. Skipping adjustments for all users.");
            return;
        }
        BigDecimal inflationMultiplier = BigDecimal.valueOf(1 + (growthRate / 100.0));
        log.info("Scheduled job: Applying CPI growth rate of {}% (Multiplier: {})", growthRate, inflationMultiplier);

        for (UserInfoEntity user : allUsers) {
            // 2. 사용자의 자동 예산 조정 설정 확인
            if (user.getAutoBudgetAdjustmentEnabled() != null && user.getAutoBudgetAdjustmentEnabled()) {
                log.info("Scheduled job: Adjusting budgets for user: {}", user.getUserId());

                // 현재 월의 사용자 예산 조회
                YearMonth currentMonth = YearMonth.now();
                List<BudgetEntity> userBudgets = budgetRepository.findAllByUserIdAndYearAndMonth(
                        user.getUserId(), currentMonth.getYear(), currentMonth.getMonthValue()
                );

                if (userBudgets.isEmpty()) {
                    log.info("Scheduled job: User {} has no budgets for the current month ({}). Skipping adjustment.", user.getUserId(), currentMonth);
                    continue;
                }

                StringBuilder emailContent = new StringBuilder();
                emailContent.append("안녕하세요, SaveMate 입니다.<br><br>");
                emailContent.append("이번 달 물가 변동을 반영하여 예산이 자동 조정되었습니다.<br>");
                emailContent.append(String.format("적용된 물가 상승률: %.2f%%<br><br>", growthRate));
                emailContent.append("조정된 예산 내역:<br>");
                emailContent.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
                emailContent.append("<thead><tr><th>카테고리</th><th>원래 예산</th><th>조정된 예산</th></tr></thead><tbody>");

                boolean adjustedAnyBudget = false;
                for (BudgetEntity budget : userBudgets) {
                    BigDecimal originalBudget = budget.getTotalBudget();
                    BigDecimal adjustedBudget = originalBudget.multiply(inflationMultiplier).setScale(0, RoundingMode.HALF_UP);

                    log.info("Scheduled job: Adjusting budget for user {} - category '{}': From {} to {}",
                            user.getUserId(), budget.getCategory(), originalBudget, adjustedBudget);

                    budget.updateBudgetInfo(budget.getYear(), budget.getMonth(), budget.getCategory(), adjustedBudget);
                    budgetRepository.save(budget);

                    emailContent.append(String.format("<tr><td>%s</td><td>%s원</td><td>%s원</td></tr>",
                            budget.getCategory(), originalBudget.toPlainString(), adjustedBudget.toPlainString()));
                    adjustedAnyBudget = true;
                }
                emailContent.append("</tbody></table><br>");
                emailContent.append("SaveMate와 함께 현명한 소비 생활을 이어가세요!<br>");

                // 예산이 실제로 조정된 경우에만 이메일 발송
                if (adjustedAnyBudget) {
                    try {
                        // 사용자 이메일 주소 가져오기 (UserInfoRepository 필요)
                        Optional<UserInfoEntity> userOpt = userInfoRepository.findByUserId(user.getUserId());
                        if (userOpt.isPresent()) {
                            String userEmail = userOpt.get().getEmail();
                            mailService.doSendMail(MailDTO.builder()
                                    .toMail(userEmail)
                                    .title("[SaveMate] 월간 예산 자동 조정 결과 안내")
                                    .contents(emailContent.toString())
                                    .build());
                            log.info("Scheduled job: Sent budget adjustment email to user {}: {}", user.getUserId(), userEmail);
                        } else {
                            log.warn("Scheduled job: User email not found for user {}. Cannot send adjustment email.", user.getUserId());
                        }
                    } catch (Exception e) {
                        log.error("Scheduled job: Failed to send budget adjustment email to user {}: {}", user.getUserId(), e.getMessage(), e);
                    }
                }

                log.info("Scheduled job: Finished adjusting budgets for user: {}.", user.getUserId());
            } else {
                log.info("Scheduled job: Skipping user {} as auto-adjustment is disabled.", user.getUserId());
            }
        }

        log.info("END: Scheduled job adjustBudgetsBasedOnInflation");
    }
}