package kopo.newproject.repository.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

/**
 * 성별 및 연령대별로 집계된 평균 소비 데이터를 저장하는 MongoDB 엔티티.
 * <p>
 * 이 엔티티는 MongoDB의 'aggregated_spending' 컬렉션에 매핑됩니다.
 * 주로 AI 분석 서비스에서 다른 사용자 그룹과의 소비 패턴 비교를 위해 사용됩니다.
 * <p>
 * {@code @Document(collection = "aggregated_spending")} - 엔티티가 매핑될 MongoDB 컬렉션의 이름을 지정합니다.
 * {@code @Getter} - Lombok 어노테이션으로, 모든 필드에 대한 getter 메소드를 자동으로 생성합니다.
 * {@code @Setter} - Lombok 어노테이션으로, 모든 필드에 대한 setter 메소드를 자동으로 생성합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "aggregated_spending")
@Builder
public class AggregatedSpendingEntity {

    /**
     * 집계 데이터의 고유 ID (Primary Key).
     * 일반적으로 "연월_성별_연령대" 형식의 복합 키로 구성됩니다 (예: "2023-01_M_20대").
     */
    @Id
    private String id;

    /**
     * 집계된 데이터의 기준 연월.
     */
    private YearMonth month;

    /**
     * 집계된 그룹의 성별 ("M" 또는 "F").
     */
    private String gender;

    /**
     * 집계된 그룹의 연령대 (예: "10대", "20대", "30대").
     */
    private String ageGroup;

    /**
     * 해당 그룹의 카테고리별 평균 지출액을 저장하는 Map.
     * Key: 카테고리명 (String), Value: 평균 지출액 (BigDecimal).
     */
    private Map<String, BigDecimal> categoryAverageSpending;

    /**
     * 해당 그룹에 속하며, 집계에 포함된 사용자들의 수.
     */
    private long userCount;
}