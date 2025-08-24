package kopo.newproject.dto;

import java.math.BigDecimal;

public record SpendingTotalDTO(
        /**
         * 지출 총액과 같은 집계된 금액을 담기 위한 DTO(Data Transfer Object).
         * <p>
         * 주로 데이터베이스의 집계(Aggregation) 쿼리 결과(예: {@code $sum} 연산의 결과)를 매핑하는 데 사용됩니다.
         * Java 16부터 도입된 {@code record} 타입을 사용하여 간결하고 불변(immutable)한 데이터 클래스를 정의합니다.
         */
        BigDecimal total
) {
}