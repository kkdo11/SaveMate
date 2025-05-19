package kopo.newproject.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter

@Setter
@Data
public class SpendingRequest {

    @NotBlank(message = "이름은 필수 항목입니다.")  // 이름이 비어있지 않아야 함
    private String name;

    @NotNull(message = "날짜는 필수 항목입니다.")  // 날짜가 Null일 수 없음
    private LocalDate date;

    @NotBlank(message = "카테고리는 필수 항목입니다.")  // 카테고리 필수
    private String category;

    @Min(value = 1, message = "금액은 1원 이상이어야 합니다.")  // 금액은 1원 이상
    private BigDecimal amount;

    @NotBlank(message = "설명은 필수 항목입니다.")  // 설명은 필수
    private String description;

    private LocalDateTime createdAt;
}
