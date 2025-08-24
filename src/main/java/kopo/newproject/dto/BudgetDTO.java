package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * 예산(Budget) 정보를 계층 간에 전달하기 위한 DTO(Data Transfer Object).
 * <p>
 * 컨트롤러와 서비스 계층, 그리고 프론트엔드 간에 예산 데이터를 주고받을 때 사용됩니다.
 * <p>
 * {@code @Data} - Lombok 어노테이션으로, {@code @Getter}, {@code @Setter}, {@code @ToString}, {@code @EqualsAndHashCode}, {@code @RequiredArgsConstructor}를 포함합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 * {@code @JsonInclude(JsonInclude.Include.NON_DEFAULT)} - JSON 직렬화 시, 필드의 값이 기본값(예: int의 0, boolean의 false, 객체의 null)과 같으면 해당 필드를 JSON 출력에서 제외합니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BudgetDTO {

    /**
     * 예산의 고유 ID.
     * JSON 직렬화/역직렬화 시 "budget_id" 필드에 매핑됩니다.
     */
    @JsonProperty("budget_id")
    private Long budgetId;

    /**
     * 예산을 설정한 사용자의 ID.
     * JSON 직렬화/역직렬화 시 "user_id" 필드에 매핑됩니다.
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 예산이 설정된 연도.
     */
    private int year;

    /**
     * 예산이 설정된 월.
     */
    private int month;

    /**
     * 예산의 카테고리 (예: "식비", "교통비").
     */
    private String category;

    /**
     * 설정된 총 예산 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     * JSON 직렬화/역직렬화 시 "total_budget" 필드에 매핑됩니다.
     */
    @JsonProperty("total_budget")
    private BigDecimal totalBudget;

    /**
     * 해당 예산 카테고리에서 현재까지 사용된 금액.
     * 이 필드는 주로 계산되어 DTO에 포함되며, 데이터베이스에 직접 저장되지 않을 수 있습니다.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     * JSON 직렬화/역직렬화 시 "used_budget" 필드에 매핑됩니다.
     */
    @JsonProperty("used_budget")
    private BigDecimal usedBudget;

    /**
     * 남은 예산 금액.
     * 이 필드는 주로 계산되어 DTO에 포함되며, 데이터베이스에 직접 저장되지 않을 수 있습니다.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     * JSON 직렬화/역직렬화 시 "remaining_budget" 필드에 매핑됩니다.
     */
    @JsonProperty("remaining_budget")
    private BigDecimal remainingBudget;
}
