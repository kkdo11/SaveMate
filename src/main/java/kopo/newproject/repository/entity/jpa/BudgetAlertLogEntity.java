package kopo.newproject.repository.entity.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/**
 * 예산 초과 예측 알림 발송 기록을 저장하는 JPA 엔티티.
 * <p>
 * 이 엔티티는 관계형 데이터베이스의 'BUDGET_ALERT_LOG' 테이블에 매핑됩니다.
 * 알림이 특정 사용자, 특정 연월, 특정 카테고리에 대해 한 번만 발송되도록 중복 발송을 방지하는 데 사용됩니다.
 * <p>
 * {@code @Entity} - 이 클래스가 JPA 엔티티임을 나타냅니다.
 * {@code @Table(name = "BUDGET_ALERT_LOG")} - 엔티티가 매핑될 데이터베이스 테이블의 이름을 지정합니다.
 * {@code @Getter} - Lombok 어노테이션으로, 모든 필드에 대한 getter 메소드를 자동으로 생성합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 * {@code @DynamicInsert} - 엔티티 저장 시, null이 아닌 필드만 SQL INSERT 문에 포함되도록 합니다.
 * {@code @DynamicUpdate} - 엔티티 업데이트 시, 변경된 필드만 SQL UPDATE 문에 포함되도록 합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "BUDGET_ALERT_LOG")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class BudgetAlertLogEntity {

    /**
     * 알림 로그의 고유 ID (Primary Key).
     * 데이터베이스에서 자동으로 생성되는 IDENTITY 전략을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    /**
     * 알림을 받은 사용자의 ID.
     * 'user_id' 컬럼에 매핑되며, null을 허용하지 않습니다.
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * 알림이 발송된 예산의 연도.
     * 'year' 컬럼에 매핑되며, null을 허용하지 않습니다.
     */
    @Column(name = "year", nullable = false)
    private int year;

    /**
     * 알림이 발송된 예산의 월.
     * 'month' 컬럼에 매핑되며, null을 허용하지 않습니다.
     */
    @Column(name = "month", nullable = false)
    private int month;

    /**
     * 알림이 발송된 예산의 카테고리.
     * 'category' 컬럼에 매핑되며, null을 허용하지 않습니다.
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * 알림이 발송된 시각.
     * 'sent_at' 컬럼에 매핑되며, null을 허용하지 않습니다.
     */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}