
package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class BokApiResponse {

    @JsonProperty("StatisticSearch")
    private StatisticSearch statisticSearch;

    @Data
    public static class StatisticSearch {
        @JsonProperty("list_total_count")
        private int listTotalCount;

        @JsonProperty("row")
        private List<StatisticRow> row;
    }
}
