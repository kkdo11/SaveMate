package kopo.newproject.repository.entity.mongo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 사용자의 개별 지출 내역을 저장하는 MongoDB 엔티티.
 * <p>
 * 이 엔티티는 MongoDB의 'spending' 컬렉션에 매핑됩니다.
 * 각 지출 항목의 상세 정보를 기록합니다.
 * <p>
 * {@code @Document(collection = "spending")} - 엔티티가 매핑될 MongoDB 컬렉션의 이름을 지정합니다.
 * {@code @Data} - Lombok 어노테이션으로, {@code @Getter}, {@code @Setter}, {@code @ToString}, {@code @EqualsAndHashCode}, {@code @RequiredArgsConstructor}를 포함합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 */
@Document(collection = "spending")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingEntity {

    /**
     * 지출 내역의 고유 ID (Primary Key).
     * MongoDB의 {@link ObjectId} 타입이며, JSON 직렬화 시 문자열로 변환됩니다.
     * {@code @Id} - MongoDB 문서의 기본 키 필드를 나타냅니다.
     * {@code @JsonSerialize(using = ToStringSerializer.class)} - ObjectId를 JSON으로 변환할 때 문자열 형태로 변환하도록 지정합니다.
     */
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    /**
     * 지출을 기록한 사용자의 ID.
     */
    private String userId;

    /**
     * 지출 항목의 이름 또는 간략한 설명 (예: "점심 식사", "커피").
     */
    private String name;

    /**
     * 지출이 발생한 날짜.
     */
    private LocalDate date;

    /**
     * 지출의 카테고리 (예: "식비", "교통", "문화생활").
     */
    private String category;

    /**
     * 지출 금액..
     * {@code BigDecimal}을 사용하여 정확한 금액 계산을 보장합니다.
     */
    private BigDecimal amount;

    /**
     * 지출에 대한 상세 설명.
     */
    private String description;
}