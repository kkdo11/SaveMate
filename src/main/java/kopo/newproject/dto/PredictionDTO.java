package kopo.newproject.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
public class PredictionDTO {
    private BigDecimal totalPredictedAmount;
    private Map<String, BigDecimal> categoryPredictedAmounts;
    private String message;
}
