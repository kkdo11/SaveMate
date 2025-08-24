package kopo.newproject.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 재정 목표(Goal) 정보를 계층 간에 전달하기 위한 DTO(Data Transfer Object).
 * <p>
 * 컨트롤러와 서비스 계층, 그리고 프론트엔드 간에 재정 목표 데이터를 주고받을 때 사용됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDTO {

    /**
     * 재정 목표의 고유 ID.
     * 주로 응답 시 클라이언트에 목표를 식별하기 위해 사용됩니다.
     */
    private Long goalId;

    /**
     * 재정 목표를 설정한 사용자의 ID.
     * 요청 및 응답 시 모두 사용되는 공통 필드입니다.
     */
    private String userId;

    /**
     * 재정 목표의 이름 또는 설명.
     * 요청 및 응답 시 모두 사용되는 공통 필드입니다.
     */
    private String goalName;

    /**
     * 목표 달성 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     * 요청 및 응답 시 모두 사용되는 공통 필드입니다.
     */
    private BigDecimal targetAmount;

    /**
     * 현재까지 저축된 금액.
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     * 요청 및 응답 시 모두 사용되는 공통 필드입니다.
     */
    private BigDecimal savedAmount;

    /**
     * 목표 달성 마감일.
     * 요청 및 응답 시 모두 사용되는 공통 필드입니다.
     */
    private LocalDate deadline;

    /**
     * 목표 달성까지 남은 금액.
     * 이 필드는 주로 서비스 계층에서 계산되어 DTO에 포함되며, 응답 시 사용됩니다。
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     */
    private BigDecimal remainingAmount;

    /**
     * 목표 달성률 (백분율).
     * 이 필드는 주로 서비스 계층에서 계산되어 DTO에 포함되며, 응답 시 사용됩니다。
     * {@code BigDecimal}을 사용하여 정확한 계산을 보장합니다.
     */
    private BigDecimal progressRate;
}