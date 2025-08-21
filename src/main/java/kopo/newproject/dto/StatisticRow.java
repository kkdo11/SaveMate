
package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StatisticRow {

    @JsonProperty("STAT_CODE")
    private String statCode; // 통계표 코드

    @JsonProperty("STAT_NAME")
    private String statName; // 통계명

    @JsonProperty("ITEM_CODE1")
    private String itemCode1; // 항목코드1

    @JsonProperty("ITEM_NAME1")
    private String itemName1; // 항목명1

    @JsonProperty("UNIT_NAME")
    private String unitName; // 단위

    @JsonProperty("TIME")
    private String time; // 시점 (e.g., "202301")

    @JsonProperty("DATA_VALUE")
    private String dataValue; // 데이터 값
}
