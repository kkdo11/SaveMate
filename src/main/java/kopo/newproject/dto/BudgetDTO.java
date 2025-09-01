package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BudgetDTO {

    @JsonProperty("budget_id")
    private Long budgetId;

    @JsonProperty("user_id")
    private String userId;

    private int year;
    private int month;

    private String category;

    @JsonProperty("total_budget")
    private BigDecimal totalBudget;

    @JsonProperty("used_budget")
    private BigDecimal usedBudget;

    @JsonProperty("remaining_budget")
    private BigDecimal remainingBudget;

    @JsonProperty("last_adjusted_date")
    private LocalDateTime lastAdjustedDate;
}