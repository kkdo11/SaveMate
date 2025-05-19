package kopo.newproject.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.newproject.repository.entity.mongo.AIAnalysisEntity;
import kopo.newproject.repository.mongo.AIAnalysisRepository;

import kopo.newproject.service.IAIAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService implements IAIAnalysisService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AIAnalysisRepository aiAnalysisRepository;
    private final AnalysisPreprocessorService preprocessorService;

    @Value("${openai.api.url}")
    private String openAiUrl;

    @Value("${openai.api.key}")
    private String openAiKey;

    @Override
    public String analyzeUserSpending(String userId, YearMonth yearMonth, Map<String, Object> preprocessedData) {
        try {
            String requestJson = objectMapper.writeValueAsString(preprocessedData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiKey);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4",
                    "messages", new Object[]{
                            Map.of("role", "system", "content", "당신은 소비 분석 AI입니다."),
                            Map.of("role", "user", "content", generatePrompt(preprocessedData))
                    },
                    "temperature", 0.7
            );

            HttpEntity<?> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    openAiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 🧠 GPT 응답 content 추출
            String content = ((Map)((Map)((List<?>) response.getBody().get("choices")).get(0)).get("message")).get("content").toString();

            // 🧼 JSON 블록만 추출
            int start = content.indexOf("{");
            int end = content.lastIndexOf("}");
            if (start == -1 || end == -1 || start >= end) {
                throw new IllegalArgumentException("GPT 응답에서 JSON 블럭을 찾을 수 없습니다.");
            }

            String cleanJson = content.substring(start, end + 1);

            // ✅ JSON 파싱
            Map<String, String> parsed = objectMapper.readValue(cleanJson, new TypeReference<>() {});

            // 📝 DB 저장
            AIAnalysisEntity analysis = AIAnalysisEntity.builder()
                    .userId(userId)
                    .month(yearMonth.toString())
                    .requestData(requestJson)
                    .result(objectMapper.writeValueAsString(parsed)) // JSON 형태로 저장
                    .createdAt(LocalDateTime.now())
                    .build();

            aiAnalysisRepository.save(analysis);

            // 🔁 JSON 문자열 반환
            return objectMapper.writeValueAsString(parsed);

        } catch (Exception e) {
            log.error("GPT 요청 실패", e);
            return "❌ GPT 분석 실패: " + e.getMessage();
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

💾 사용자 데이터:
%s

📐 JSON 응답 형식과 작성 가이드:

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
    public String analyze(String userId, String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr);
        Map<String, Object> data = preprocessorService.generateAnalysisInput(userId, yearMonth);
        return analyzeUserSpending(userId, yearMonth, data);
    }

    @Override
    public List<AIAnalysisEntity> getAnalysisByMonth(String userId, String yearMonth) {
        return aiAnalysisRepository.findByUserIdAndMonth(userId, yearMonth);
    }

    @Override
    public void deleteAnalysisByMonth(String userId, String yearMonth) {
        aiAnalysisRepository.deleteByUserIdAndMonth(userId, yearMonth);
    }
}
