package kopo.newproject.repository.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@Cacheable
@Entity
@Table(name = "GOALS")
public class GoalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MariaDB AUTO_INCREMENT와 호환
    @Column(name = "goal_id")
    private Long goalId;

    @NonNull
    @Column(name = "user_id", length = 50, nullable = false)
    private String userId;

    @NonNull
    @Column(name = "goal_name", length = 255, nullable = false)
    private String goalName;

    @Column(name = "target_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal targetAmount = BigDecimal.ZERO;

    @Column(name = "saved_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal savedAmount = BigDecimal.ZERO;

    @Column(name = "deadline")
    private LocalDate deadline;

    // 계산용 임시 필드 (DB에 저장되지 않음)
    @Transient
    @Setter
    private BigDecimal progressRate;

    @Transient
    public BigDecimal getRemainingAmount() {
        return Optional.ofNullable(targetAmount).orElse(BigDecimal.ZERO)
                .subtract(Optional.ofNullable(savedAmount).orElse(BigDecimal.ZERO));
    }

    @Transient
    public BigDecimal getProgressRate() {
        BigDecimal target = Optional.ofNullable(targetAmount).orElse(BigDecimal.ZERO);
        if (target.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return Optional.ofNullable(savedAmount).orElse(BigDecimal.ZERO)
                .divide(target, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
