package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 한국은행(BOK) 경제통계시스템(ECOS) API에서 반환되는 통계 데이터의 한 행(Row)을 담는 DTO.
 * <p>
 * 각 필드는 API 응답의 JSON 키에 직접 매핑됩니다.
 * <p>
 * {@code @Data} - Lombok 어노테이션으로, {@code @Getter}, {@code @Setter}, {@code @ToString}, {@code @EqualsAndHashCode}, {@code @RequiredArgsConstructor}를 포함합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // 일관성을 위해 Builder 추가
public class StatisticRow {

    /**
     * 통계표 코드.
     * JSON 응답의 "STAT_CODE" 필드에 매핑됩니다.
     */
    @JsonProperty("STAT_CODE")
    private String statCode;

    /**
     * 통계명.
     * JSON 응답의 "STAT_NAME" 필드에 매핑됩니다.
     */
    @JsonProperty("STAT_NAME")
    private String statName;

    /**
     * 항목코드1.
     * JSON 응답의 "ITEM_CODE1" 필드에 매핑됩니다.
     */
    @JsonProperty("ITEM_CODE1")
    private String itemCode1;

    /**
     * 항목명1.
     * JSON 응답의 "ITEM_NAME1" 필드에 매핑됩니다.
     */
    @JsonProperty("ITEM_NAME1")
    private String itemName1;

    /**
     * 데이터 단위.
     * JSON 응답의 "UNIT_NAME" 필드에 매핑됩니다.
     */
    @JsonProperty("UNIT_NAME")
    private String unitName;

    /**
     * 데이터 시점 (예: "202301" - 2023년 1월).
     * JSON 응답의 "TIME" 필드에 매핑됩니다.
     */
    @JsonProperty("TIME")
    private String time;

    /**
     * 실제 통계 데이터 값.
     * {@code String} 타입으로 제공되므로, 사용 시 적절한 숫자 타입(예: {@code BigDecimal})으로 변환해야 합니다.
     * JSON 응답의 "DATA_VALUE" 필드에 매핑됩니다.
     */
    @JsonProperty("DATA_VALUE")
    private String dataValue;
}