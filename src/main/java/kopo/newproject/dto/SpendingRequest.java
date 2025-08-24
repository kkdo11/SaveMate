package kopo.newproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 지출 내역 생성 또는 수정 요청에 필요한 정보를 담는 DTO(Data Transfer Object).
 * <p>
 * 클라이언트로부터 지출 데이터를 받아 서비스 계층으로 전달하기 전에,
 * {@code jakarta.validation.constraints} 패키지의 어노테이션을 사용하여 입력 값의 유효성을 검증합니다.
 * <p>
 * {@code @Data} - Lombok 어노테이션으로, {@code @Getter}, {@code @Setter}, {@code @ToString}, {@code @EqualsAndHashCode}, {@code @RequiredArgsConstructor}를 포함합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SpendingRequest {

    /**
     * 지출 항목의 이름 또는 간략한 설명.
     * {@code @NotBlank(message = "이름은 필수 항목입니다.")} - 이 필드는 null이 아니어야 하며, 공백 문자열만으로 구성되어서도 안 됩니다.
     */
    @NotBlank(message = "이름은 필수 항목입니다.")
    private String name;

    /**
     * 지출이 발생한 날짜.
     * {@code @NotNull(message = "날짜는 필수 항목입니다.")} - 이 필드는 null이 아니어야 합니다.
     */
    @NotNull(message = "날짜는 필수 항목입니다.")
    private LocalDate date;

    /**
     * 지출의 카테고리 (예: "식비", "교통", "문화생활").
     * {@code @NotBlank(message = "카테고리는 필수 항목입니다.")} - 이 필드는 null이 아니어야 하며, 공백 문자열만으로 구성되어서도 안 됩니다.
     */
    @NotBlank(message = "카테고리는 필수 항목입니다.")
    private String category;

    /**
     * 지출 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     * {@code @Min(value = 1, message = "금액은 1원 이상이어야 합니다.")} - 이 필드의 값은 최소 1 이상이어야 합니다.
     */
    @Min(value = 1, message = "금액은 1원 이상이어야 합니다.")
    private BigDecimal amount;

    /**
     * 지출에 대한 상세 설명.
     * {@code @NotBlank(message = "설명은 필수 항목입니다.")} - 이 필드는 null이 아니어야 하며, 공백 문자열만으로 구성되어서도 안 됩니다.
     */
    @NotBlank(message = "설명은 필수 항목입니다.")
    private String description;

    /**
     * 지출 내역이 생성된 일시.
     * (NOTE: 이 필드는 일반적으로 시스템에서 자동으로 생성되므로, 요청 DTO에 포함되지 않는 것이 일반적입니다.
     * 엔티티에서 {@code @CreatedDate}와 같은 어노테이션을 사용하여 자동 생성하는 것을 권장합니다.)
     */
    private LocalDateTime createdAt;
}