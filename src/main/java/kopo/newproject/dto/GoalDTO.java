package kopo.newproject.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDTO {

    // 응답 시 포함될 필드
    private Long goalId;

    // 공통 필드 (요청/응답 모두 사용)
    private String userId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private LocalDate deadline;

    // 계산용 필드 (응답용)
    private BigDecimal remainingAmount;
    private BigDecimal progressRate;
}
