package kopo.newproject.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 다음 달 소비 예측 결과를 담는 DTO(Data Transfer Object).
 * <p>
 * AI 분석 서비스에서 생성된 예측 데이터를 컨트롤러나 프론트엔드로 전달하는 데 사용됩니다.
 */
@Getter
@Setter
@Builder
public class PredictionDTO {
    /**
     * 다음 달 총 예상 지출 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     */
    private BigDecimal totalPredictedAmount;

    /**
     * 다음 달 카테고리별 예상 지출 금액을 담은 Map.
     * Key: 카테고리명 (String), Value: 예상 지출액 (BigDecimal).
     */
    private Map<String, BigDecimal> categoryPredictedAmounts;

    /**
     * 예측 결과에 대한 메시지.
     * (예: "예측 완료", "데이터 부족으로 예측 불가" 등)
     */
    private String message;
}