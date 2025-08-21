package kopo.newproject.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            ResponseEntity<String> response = restTemplate.exchange(openAiUrl, HttpMethod.POST, entity, String.class);

            String content = response.getBody();
            int start = content.indexOf("{");
            int end = content.lastIndexOf("}");
            String cleanJson = content.substring(start, end + 1);

            Map<String, String> parsed = objectMapper.readValue(cleanJson, new TypeReference<>() {});

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
        return String.format("""
Analyze the following user spending data and provide insights. The response must be in JSON format.

User Data:
%s

JSON Response Format:
{
  "summary": "Monthly summary...",
  "habit": "Spending habit analysis...",
  "tip": "Savings tips...",
  "anomaly": "Anomaly detection...",
  "guide": "Next month's guide..."
}
""", data.toString());
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