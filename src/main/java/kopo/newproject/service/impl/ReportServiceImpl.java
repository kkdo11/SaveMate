package kopo.newproject.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.newproject.dto.MailDTO;
import kopo.newproject.dto.MonthlyReportDTO;
import kopo.newproject.dto.GoalDTO;
import kopo.newproject.dto.UserInfoDTO;
import kopo.newproject.repository.entity.jpa.BudgetEntity;
import kopo.newproject.repository.entity.jpa.GoalEntity;
import kopo.newproject.repository.entity.jpa.UserInfoEntity;
import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import kopo.newproject.service.IAIAnalysisService;
import kopo.newproject.service.IBudgetService;
import kopo.newproject.service.IGoalService;
import kopo.newproject.service.IMailService;
import kopo.newproject.service.IReportService;
import kopo.newproject.service.ISpendingService;
import kopo.newproject.service.IUserInfoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.*;
import kopo.newproject.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.stream.Collectors;
import java.time.YearMonth;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportServiceImpl implements IReportService {

    private final IUserInfoService userInfoService;
    private final ISpendingService spendingService;
    private final IBudgetService budgetService;
    private final IGoalService goalService;
    private final IAIAnalysisService aiAnalysisService;
    private final IMailService mailService;
    private final TemplateEngine templateEngine;

    @Override
    public MonthlyReportDTO generateMonthlyReportData(String userId, YearMonth reportMonth) throws Exception {
        log.info("리포트 데이터 생성 시작: userId={}, month={}", userId, reportMonth);

        // 리포트의 기준이 되는 월과 이전 월
        YearMonth previousMonth = reportMonth.minusMonths(1);

        // 1. 기본 사용자 정보
        UserInfoDTO userInfo = userInfoService.findByUserId(userId);

        // 2. 소비 데이터
        BigDecimal totalSpending = spendingService.calculateMonthlySpendingSum(userId, reportMonth.getYear(), reportMonth.getMonthValue(), null);
        BigDecimal previousMonthTotalSpending = spendingService.calculateMonthlySpendingSum(userId, previousMonth.getYear(), previousMonth.getMonthValue(), null);
        Map<String, BigDecimal> spendingByCategory = spendingService.getSpendingByCategory(userId, reportMonth);

        // ======================= DEBUG LOGGING START =======================
        log.info("[DEBUG] Report Data for userId: {}, reportMonth: {}", userId, reportMonth);
        log.info("[DEBUG] totalSpending: {}", totalSpending);
        log.info("[DEBUG] spendingByCategory Map: {}", spendingByCategory);
        // ======================= DEBUG LOGGING END =======================

        // 3. 예산 데이터
        List<BudgetEntity> budgets = budgetService.getBudgetsByUserIdAndYearMonth(userId, reportMonth.getYear(), reportMonth.getMonthValue());
        BigDecimal totalBudget = budgets.stream().map(BudgetEntity::getTotalBudget).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 이전 달 예산 데이터
        List<BudgetEntity> previousMonthBudgets = budgetService.getBudgetsByUserIdAndYearMonth(userId, previousMonth.getYear(), previousMonth.getMonthValue());
        BigDecimal previousMonthTotalBudget = previousMonthBudgets.stream().map(BudgetEntity::getTotalBudget).reduce(BigDecimal.ZERO, BigDecimal::add);


        // 4. 목표 데이터
        List<GoalDTO> goalDTOs = goalService.getGoalsByUser(userId);
        List<GoalEntity> goals = goalDTOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());

        // 5. AI 분석 데이터
        AIAnalysisEntity analysis = aiAnalysisService.getAnalysisByMonth(userId, reportMonth.toString());

        // 6. 데이터 가공 및 DTO 필드 계산
        BigDecimal spendingChangePercentage = calculatePercentageChange(previousMonthTotalSpending, totalSpending);
        BigDecimal budgetChangePercentage = calculatePercentageChange(previousMonthTotalBudget, totalBudget);
        BigDecimal budgetAchievementRate = calculateAchievementRate(totalSpending, totalBudget);
        List<MonthlyReportDTO.CategorySpendingDTO> topSpendingCategories = calculateTopSpendingCategories(spendingByCategory, totalSpending);
        List<MonthlyReportDTO.GoalStatusDTO> goalStatuses = calculateGoalStatuses(goals);
        String aiSummary = (analysis != null) ? extractSummaryFromResult(analysis.getResult()) : "이번 달 AI 분석 내역이 없습니다.";

        // 최종 DTO 생성 및 반환
        return MonthlyReportDTO.builder()
                .userName(userInfo.name())
                .userEmail(userInfo.email())
                .reportMonth(reportMonth)
                .totalSpending(totalSpending)
                .previousMonthTotalSpending(previousMonthTotalSpending)
                .spendingChangePercentage(spendingChangePercentage)
                .totalBudget(totalBudget)
                .previousMonthTotalBudget(previousMonthTotalBudget)
                .budgetChangePercentage(budgetChangePercentage)
                .budgetAchievementRate(budgetAchievementRate)
                .topSpendingCategories(topSpendingCategories)
                .spendingByCategory(spendingByCategory)
                .goalStatuses(goalStatuses)
                .aiSummary(aiSummary)
                .build();
    }

    // --- 데이터 가공 헬퍼 메소드 ---

    private BigDecimal calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return (current.compareTo(BigDecimal.ZERO) > 0) ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous).divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateAchievementRate(BigDecimal actual, BigDecimal target) {
        if (target == null || target.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return actual.divide(target, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    private List<MonthlyReportDTO.CategorySpendingDTO> calculateTopSpendingCategories(Map<String, BigDecimal> spendingByCategory, BigDecimal totalSpending) {
        if (totalSpending.compareTo(BigDecimal.ZERO) == 0) return Collections.emptyList();

        return spendingByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .map(entry -> MonthlyReportDTO.CategorySpendingDTO.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .percentage(entry.getValue().divide(totalSpending, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<MonthlyReportDTO.GoalStatusDTO> calculateGoalStatuses(List<GoalEntity> goals) {
        return goals.stream().map(goal -> {
            BigDecimal savedAmount = goal.getSavedAmount() != null ? goal.getSavedAmount() : BigDecimal.ZERO;
            BigDecimal targetAmount = goal.getTargetAmount();
            return MonthlyReportDTO.GoalStatusDTO.builder()
                    .goalName(goal.getGoalName())
                    .targetAmount(targetAmount)
                    .savedAmount(savedAmount)
                    .achievementRate(calculateAchievementRate(savedAmount, targetAmount).doubleValue())
                    .isAchieved(savedAmount.compareTo(targetAmount) >= 0)
                    .build();
        }).collect(Collectors.toList());
    }

    private String extractSummaryFromResult(String jsonResult) {
        // 실제로는 JSON 파싱을 통해 "summary" 필드를 추출해야 함
        // 여기서는 간단하게 구현
        if (jsonResult != null && jsonResult.contains("summary")) {
            // 매우 간단한 파싱 로직, 실제로는 ObjectMapper 사용 권장
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> resultMap = mapper.readValue(jsonResult, new TypeReference<Map<String, String>>() {});
                return resultMap.getOrDefault("summary", "분석 요약을 가져올 수 없습니다.");
            } catch (Exception e) {
                log.error("AI 분석 결과 파싱 실패", e);
                return "AI 분석 결과 요약에 실패했습니다.";
            }
        }
        return "이번 달 AI 분석 요약이 없습니다.";
    }

    private GoalEntity convertToEntity(GoalDTO dto) {
        return GoalEntity.builder()
                .goalId(dto.getGoalId())
                .userId(dto.getUserId())
                .goalName(dto.getGoalName())
                .targetAmount(dto.getTargetAmount())
                .savedAmount(dto.getSavedAmount())
                .deadline(dto.getDeadline())
                .build();
    }

    @Override
    public void generateAndSendReportForUser(String userId, YearMonth reportMonth) throws Exception {
        log.info("사용자 {}의 리포트 생성 및 발송 시작...", userId);
        try {
            MonthlyReportDTO reportData = generateMonthlyReportData(userId, reportMonth);
            String htmlContent = buildReportHtml(reportData);

            MailDTO mail = MailDTO.builder()
                    .toMail(reportData.getUserEmail())
                    .title(String.format("[%s] %d년 %d월 월간 소비 리포트",
                            "SaveMate", reportMonth.getYear(), reportMonth.getMonthValue()))
                    .contents(htmlContent)
                    .build();

            mailService.doSendMail(mail);
            log.info("사용자 {}에게 리포트 이메일 발송 완료", userId);
        } catch (Exception e) {
            log.error("사용자 {}의 월간 리포트 생성 또는 발송 중 오류 발생", userId, e);
            throw e; // re-throw the exception to be handled by the caller if necessary
        }
    }

    @Scheduled(cron = "0 0 22 L * ?") // 매월 말일 22시
    @Override
    public void sendMonthlyReportToAllUsers() {
        log.info("월간 리포트 발송 스케줄러 시작");
        YearMonth reportMonth = YearMonth.now(ZoneId.of("Asia/Seoul")).minusMonths(1); // 지난달을 기준으로 리포트 생성

        List<UserInfoEntity> allUsers = null;
        try {
            allUsers = userInfoService.getAllUsers();
        } catch (Exception e) {
            log.error("리포트 발송을 위한 사용자 목록 조회 중 오류 발생", e);
            return; // 사용자 목록 조회를 실패하면 작업을 중단
        }
        log.info("{}명의 사용자를 대상으로 리포트 발송을 시작합니다.", allUsers.size());

        for (UserInfoEntity user : allUsers) {
            try {
                generateAndSendReportForUser(user.getUserId(), reportMonth);
            } catch (Exception e) {
                // 오류는 generateAndSendReportForUser 내부에서 이미 로깅되었으므로 여기서는 다음 사용자로 넘어갑니다.
            }
        }

        log.info("월간 리포트 발송 스케줄러 종료");
    }

    private String buildReportHtml(MonthlyReportDTO reportDTO) {
        Context context = new Context();
        context.setVariable("report", reportDTO);
        return templateEngine.process("mail/monthlyReport", context);
    }
}
