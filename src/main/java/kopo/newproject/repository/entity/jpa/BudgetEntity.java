package kopo.newproject.repository.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.Optional;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BUDGET")
@DynamicInsert
@DynamicUpdate
@Builder
@Cacheable
@Entity
public class BudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //DB에서 auto increment 를 설정해놨는데 상관 없는지
    @Column(name = "budget_id")
    private Long budgetId;

    @NonNull
    @Column(name = "user_id")
    private String userId;

    @NonNull
    @Column(name = "year")
    private int year;

    @NonNull
    @Column(name = "month")
    private int month;

    @NonNull
    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @Transient
    @Setter
    private BigDecimal usedBudget;

    @NonNull
    @Column(name = "category")
    private String category;

    
    @Transient
    public BigDecimal getRemainingBudget() {
        return Optional.ofNullable(totalBudget)
                .orElse(BigDecimal.ZERO)
                .subtract(Optional.ofNullable(usedBudget).orElse(BigDecimal.ZERO));
    }

}


