package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI GPT API의 응답 데이터를 담는 DTO(Data Transfer Object).
 * <p>
 * GPT 모델로부터 받은 JSON 응답을 자바 객체로 매핑하는 데 사용됩니다.
 * <p>
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} - JSON 응답에 DTO에 정의되지 않은 필드가 포함되어 있어도
 * 역직렬화(deserialization) 시 오류를 발생시키지 않고 해당 필드를 무시하도록 합니다.
 * 이는 API 응답 구조가 변경되거나 새로운 필드가 추가될 때 유연성을 제공합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // 일관성을 위해 Builder 추가
@JsonIgnoreProperties(ignoreUnknown = true)
public class GptResponseDTO {
    /**
     * GPT 모델이 생성한 응답 선택지들의 리스트.
     * 일반적으로 하나의 요청에 대해 여러 개의 선택지가 반환될 수 있습니다.
     */
    private List<Choice> choices;

    /**
     * GPT 모델이 생성한 응답 선택지 하나를 나타내는 중첩 클래스.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder // 일관성을 위해 Builder 추가
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        /**
         * GPT 모델이 생성한 메시지 객체.
         * 실제 응답 내용(텍스트)을 담고 있습니다.
         */
        private Message message;
    }

    /**
     * GPT 모델이 생성한 메시지 내용을 담는 중첩 클래스.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder // 일관성을 위해 Builder 추가
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        /**
         * GPT 모델이 생성한 실제 텍스트 응답 내용.
         * 이 필드에 AI 분석 결과 요약, 팁 등의 텍스트가 담겨 있습니다.
         */
        private String content;
    }
}