package kopo.newproject.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.newproject.dto.GptResponseDTO;
import kopo.newproject.dto.PredictionDTO;
import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import kopo.newproject.repository.entity.mongo.SpendingEntity;
import kopo.newproject.repository.mongo.AIAnalysisRepository;
import kopo.newproject.service.IAIAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisService implements IAIAnalysisService {

    private final MongoTemplate mongoTemplate;
    private final AIAnalysisRepository aiAnalysisRepository;
    private final AnalysisPreprocessorService preprocessorService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.url}")
    private String openAiUrl;

    @Value("${openai.api.key}")
    private String openAiKey;

    // Helper class for aggregation result mapping
    public static class CategoryAverage {
        private String category;
        private BigDecimal averageAmount;
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getAverageAmount() { return averageAmount; }
        public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }
    }

    @Override
    public PredictionDTO predictNextMonthSpending(String userId) {
        log.info("Starting next month spending prediction for userId: {}", userId);

        LocalDate today = LocalDate.now();
        LocalDate threeMonthsAgo = today.minusMonths(3).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = today.withDayOfMonth(1).minusDays(1);

        // 1. Match documents for the user within the last 3 full months
        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("userId").is(userId)
                        .and("date").gte(threeMonthsAgo).lte(lastDayOfLastMonth) // Corrected field name to "date"
        );

        // 2. Project to create a yearMonth string field before grouping
        ProjectionOperation projectToCreateYearMonth = Aggregation.project("amount", "category")
                .and(DateOperators.DateToString.dateOf("date").toString("%Y-%m")).as("yearMonth"); // Corrected field name to "date"

        // 3. Group by the new yearMonth and category
        GroupOperation groupByMonthAndCategory = Aggregation.group("yearMonth", "category")
                .sum("amount").as("monthlyCategoryTotal");

        // 4. Group again by just the category from the previous group's _id
        GroupOperation groupByCategoryAndAverage = Aggregation.group("_id.category")
                .avg("monthlyCategoryTotal").as("averageAmount");

        // 5. Project the final fields to the shape of CategoryAverage class
        ProjectionOperation projectToFinalShape = Aggregation.project("averageAmount").and("_id").as("category");

        // 6. Build and run the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectToCreateYearMonth,
                groupByMonthAndCategory,
                groupByCategoryAndAverage,
                projectToFinalShape
        );

        AggregationResults<CategoryAverage> results = mongoTemplate.aggregate(aggregation, SpendingEntity.class, CategoryAverage.class);
        List<CategoryAverage> categoryAverages = results.getMappedResults();

        if (categoryAverages.isEmpty()) {
            return PredictionDTO.builder().message("최근 3개월간의 지출 내역이 부족하여 예측할 수 없습니다.").build();
        }

        Map<String, BigDecimal> categoryPredictedAmounts = new HashMap<>();
        BigDecimal totalPredictedAmount = BigDecimal.ZERO;

        for (CategoryAverage avg : categoryAverages) {
            BigDecimal roundedAmount = avg.getAverageAmount().setScale(0, RoundingMode.HALF_UP);
            categoryPredictedAmounts.put(avg.getCategory(), roundedAmount);
            totalPredictedAmount = totalPredictedAmount.add(roundedAmount);
        }

        log.info("Prediction completed for userId: {}", userId);
        return PredictionDTO.builder()
                .totalPredictedAmount(totalPredictedAmount)
                .categoryPredictedAmounts(categoryPredictedAmounts)
                .message("다음 달 소비 예측이 완료되었습니다.")
                .build();
    }

    @Override
    public String analyze(String userId, String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        Map<String, Object> data = preprocessorService.generateAnalysisInput(userId, yearMonth);
        return analyzeUserSpending(userId, yearMonth, data);
    }

    private String analyzeUserSpending(String userId, YearMonth yearMonth, Map<String, Object> preprocessedData) {
        try {
            String requestJson = objectMapper.writeValueAsString(preprocessedData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiKey);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4",
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "You are a financial analysis AI."),
                            Map.of("role", "user", "content", generatePrompt(preprocessedData))
                    }
            );

            HttpEntity<?> entity = new HttpEntity<>(body, headers);
            ResponseEntity<GptResponseDTO> response = restTemplate.exchange(openAiUrl, HttpMethod.POST, entity, GptResponseDTO.class);

            // GPT 응답에서 실제 content 추출
            String jsonContent = Optional.ofNullable(response.getBody())
                    .map(GptResponseDTO::getChoices)
                    .filter(choices -> !choices.isEmpty())
                    .map(choices -> choices.get(0).getMessage().getContent())
                    .orElseThrow(() -> new RuntimeException("GPT 응답에서 content를 찾을 수 없습니다."));

            // content 내부의 JSON 파싱
            Map<String, String> parsed = objectMapper.readValue(jsonContent, new TypeReference<>() {});

            AIAnalysisEntity analysis = AIAnalysisEntity.builder()
                    .userId(userId)
                    .month(yearMonth.toString())
                    .requestData(requestJson)
                    .result(objectMapper.writeValueAsString(parsed))
                    .createdAt(LocalDateTime.now())
                    .version(1) // Simplified versioning
                    .build();
            aiAnalysisRepository.save(analysis);

            return objectMapper.writeValueAsString(parsed);
        } catch (Exception e) {
            log.error("Error during GPT analysis: {}", e.getMessage(), e);
            throw new RuntimeException("GPT 분석 실패: " + e.getMessage());
        }
    }

    private String generatePrompt(Map<String, Object> data) {
        return """
당신은 사용자의 소비 데이터를 분석하는 전문 금융 분석 AI입니다.
목표는 소비 습관을 평가하고 절약을 위한 행동 지침을 제공하는 것입니다.

❗ 반드시 아래 조건을 따르세요:
- 결과는 오직 **JSON 형식**으로만 반환하세요 (마크다운, 코드블럭, 부가 설명 포함 금지).
- 키 이름은 영문 (summary, habit, tip, anomaly, guide) 으로 고정합니다.
- 각 키의 값은 **6~7 문장으로 구체적인 조언과 함께** 작성하세요.
- 문장은 한국어로 작성하고, **공손체/설명체로 통일**하세요.

 사용자 데이터:
%s

 JSON 응답 형식과 작성 가이드:

{
  "summary": "이 달의 예산과 총 소비 금액을 요약하고, 초과/잔여 예산이 있는 카테고리를 서술합니다.",
  "habit": "소비 습관에서 눈에 띄는 비율, 자주 지출된 항목, 반복적인 패턴 등을 분석합니다.",
  "tip": "절약을 위한 현실적인 팁 2가지 이상 제시 (구독 취소, 할인 활용 등).",
  "anomaly": "예산 초과 또는 특이 지출(비정상적 금액/날짜 등)을 식별하고 간단한 원인을 설명합니다.",
  "guide": "다음 달에 유의해야 할 행동 지침 및 소비 습관 개선 전략을 제안합니다."
}



⚠️ 반드시 위 형식을 그대로 따르세요. 추가 설명, 제목, 마크다운, 주석 없이 JSON 그 자체만 출력하세요.
""".formatted(data.toString());
    }

    @Override
    public AIAnalysisEntity getAnalysisByMonth(String userId, String yearMonth) {
        return aiAnalysisRepository.findByUserIdAndMonthOrderByCreatedAtDesc(userId, yearMonth)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void deleteAnalysisByMonth(String userId, String yearMonth) {
        aiAnalysisRepository.deleteByUserIdAndMonth(userId, yearMonth);
    }

    @Override
    public AIAnalysisEntity getLatestAnalysis(String userId) {
        return aiAnalysisRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
    }

    @Override
    public List<AIAnalysisEntity> getAnalysisHistory(String userId, String yearMonth) {
        return aiAnalysisRepository.findByUserIdAndMonthOrderByCreatedAtDesc(userId, yearMonth);
    }

    @Override
    public AIAnalysisEntity getAnalysisById(String userId, String analysisId) {
        return aiAnalysisRepository.findByIdAndUserId(analysisId, userId).orElse(null);
    }

    @Override
    public Map<String, Object> compareAnalysis(String userId, String analysisId1, String analysisId2) {
        AIAnalysisEntity a1 = getAnalysisById(userId, analysisId1);
        AIAnalysisEntity a2 = getAnalysisById(userId, analysisId2);
        if (a1 == null || a2 == null) return Map.of("error", "Analysis not found");
        try {
            Map<String, String> r1 = objectMapper.readValue(a1.getResult(), new TypeReference<>() {});
            Map<String, String> r2 = objectMapper.readValue(a2.getResult(), new TypeReference<>() {});
            Map<String, Object> diff = new HashMap<>();
            for (String key : r1.keySet()) {
                if (!Objects.equals(r1.get(key), r2.get(key))) {
                    diff.put(key, Map.of("before", r2.get(key), "after", r1.get(key)));
                }
            }
            return Map.of("differences", diff);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}