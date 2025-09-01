package kopo.newproject.repository.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime; // LocalDateTime 임포트 추가
import java.util.Optional;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BUDGET")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class BudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //DB에서 auto increment 를 설정해놨는데 상관 없는지
    @Column(name = "budget_id")
    private Long budgetId;

    @NonNull
    @Column(name = "user_id")
    private String userId;

    @Column(name = "year")
    private int year;

    
    @Column(name = "month")
    private int month;

    @NonNull
    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @Transient
    @Setter // usedBudget는 Transient 필드이므로 setter 유지
    private BigDecimal usedBudget;

    @NonNull
    @Column(name = "category")
    private String category;

    @Column(name = "last_adjusted_date") // 마지막 조정 일자 필드 추가
    private LocalDateTime lastAdjustedDate;

    
    @Transient
    public BigDecimal getRemainingBudget() {
        return Optional.ofNullable(totalBudget)
                .orElse(BigDecimal.ZERO)
                .subtract(Optional.ofNullable(usedBudget).orElse(BigDecimal.ZERO));
    }

    // ✅ 비즈니스 로직: 예산 정보 업데이트
    public void updateBudgetInfo(int year, int month, String category, BigDecimal totalBudget) {
        this.year = year;
        this.month = month;
        this.category = category;
        this.totalBudget = totalBudget;
    }

    public void setLastAdjustedDate(LocalDateTime lastAdjustedDate) {
        this.lastAdjustedDate = lastAdjustedDate;
    }

}


