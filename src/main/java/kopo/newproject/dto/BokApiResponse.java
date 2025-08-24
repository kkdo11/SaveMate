package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 한국은행(BOK) 경제통계시스템(ECOS) API의 응답 데이터를 담는 DTO(Data Transfer Object).
 * <p>
 * API 응답의 JSON 구조에 맞춰 데이터를 매핑합니다.
 * {@code @JsonProperty} 어노테이션을 사용하여 JSON 키와 Java 필드 이름을 연결합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Builder 패턴 추가 (선택 사항이지만 일관성을 위해 추가)
public class BokApiResponse {

    /**
     * 통계 검색 결과를 담고 있는 객체.
     * JSON 응답의 "StatisticSearch" 필드에 매핑됩니다.
     */
    @JsonProperty("StatisticSearch")
    private StatisticSearch statisticSearch;

    /**
     * 한국은행 API 응답 내의 통계 검색 상세 정보를 담는 중첩 클래스.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder // Builder 패턴 추가
    public static class StatisticSearch {
        /**
         * 전체 데이터 건수.
         * JSON 응답의 "list_total_count" 필드에 매핑됩니다.
         */
        @JsonProperty("list_total_count")
        private int listTotalCount;

        /**
         * 실제 통계 데이터 행(row)들의 리스트.
         * JSON 응답의 "row" 필드에 매핑됩니다. 각 행은 {@link StatisticRow} 객체로 표현됩니다.
         */
        @JsonProperty("row")
        private List<StatisticRow> row;
    }
}