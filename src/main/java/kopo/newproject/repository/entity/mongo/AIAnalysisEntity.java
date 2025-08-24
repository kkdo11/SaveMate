package kopo.newproject.repository.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * AI 기반 금융 분석 결과를 저장하는 MongoDB 엔티티.
 * <p>
 * 이 엔티티는 MongoDB의 'AI_analysis' 컬렉션에 매핑됩니다.
 * 특정 사용자의 특정 월에 대한 AI 분석 요청 데이터와 그 결과를 저장합니다.
 * <p>
 * {@code @Document(collection = "AI_analysis")} - 엔티티가 매핑될 MongoDB 컬렉션의 이름을 지정합니다.
 * {@code @Getter} - Lombok 어노테이션으로, 모든 필드에 대한 getter 메소드를 자동으로 생성합니다.
 * {@code @Setter} - Lombok 어노테이션으로, 모든 필드에 대한 setter 메소드를 자동으로 생성합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 */
@Document(collection = "AI_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisEntity {

    /**
     * AI 분석 결과의 고유 ID (Primary Key).
     * MongoDB에서 자동으로 생성되는 ObjectId 문자열입니다.
     */
    @Id
    private String id;

    /**
     * 분석을 수행한 사용자의 ID.
     */
    private String userId;

    /**
     * 분석 대상 연월 (예: "2025-08").
     */
    private String month;

    /**
     * AI 분석 모델에 전달된 원본 요청 데이터 (JSON 문자열 형태).
     * 주로 사용자의 소비/예산 데이터가 포함됩니다.
     */
    private String requestData;

    /**
     * AI 분석 모델로부터 받은 결과 (JSON 문자열 형태).
     * 분석 요약, 소비 습관, 절약 팁 등이 포함됩니다.
     */
    private String result;

    /**
     * 분석 결과의 버전.
     * AI 모델이 업데이트되거나 분석 로직이 변경될 경우, 결과의 형식을 구분하는 데 사용될 수 있습니다.
     */
    private int version;

    /**
     * 분석 결과가 생성된 일시.
     * {@code @CreatedDate} - Spring Data MongoDB 어노테이션으로, 엔티티가 저장될 때 자동으로 현재 일시가 기록됩니다.
     */
    @CreatedDate
    private LocalDateTime createdAt;
}