package kopo.newproject.dto;

import lombok.Builder;

/**
 * 작업 결과 메시지를 전달하기 위한 DTO(Data Transfer Object).
 * <p>
 * 주로 API 응답에서 작업의 성공/실패 여부와 함께 사용자에게 보여줄 메시지를 전달할 때 사용됩니다.
 * Java 16부터 도입된 {@code record} 타입을 사용하여 간결하고 불변(immutable)한 데이터 클래스를 정의합니다.
 * <p>
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 */
@Builder
public record MsgDTO(

        /**
         * 작업의 결과 코드.
         * 일반적으로 1은 성공을 의미하며, 그 외의 값은 실패 또는 특정 상태를 나타냅니다.
         */
        int result,

        /**
         * 작업 결과에 대한 상세 메시지.
         * 사용자에게 보여줄 안내 문구나 오류 메시지 등을 포함합니다.
         */
        String msg
) {
}