package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * 사용자 프로필 정보를 계층 간에 전달하기 위한 DTO(Data Transfer Object).
 * <p>
 * 회원가입, 로그인, 마이페이지 정보 조회/수정 등 사용자 관련 데이터를 주고받을 때 사용됩니다.
 * Java 16부터 도입된 {@code record} 타입을 사용하여 간결하고 불변(immutable)한 데이터 클래스를 정의합니다.
 * <p>
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 * {@code @JsonInclude(JsonInclude.Include.NON_DEFAULT)} - JSON 직렬화 시, 필드의 값이 기본값과 같으면 해당 필드를 JSON 출력에서 제외합니다.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(

        /**
         * 사용자의 고유 아이디.
         * (NOTE: 필드명은 일반적으로 camelCase를 사용하지만, 여기서는 snake_case로 정의되어 있습니다.)
         */
        @JsonProperty("user_id") // JSON 필드명과 매핑
        String user_id,

        /**
         * 사용자의 이메일 주소.
         */
        String email,

        /**
         * ❗️(주의)❗️ 사용자의 비밀번호.
         * 보안상 민감한 정보이므로, 이 DTO는 비밀번호 변경/등록과 같은 특정 목적에만 사용되어야 하며,
         * 일반적인 사용자 정보 조회 시에는 이 필드를 포함하지 않거나 null로 처리해야 합니다.
         */
        String password,

        /**
         * 사용자의 이름.
         */
        String name,

        /**
         * 사용자 계정이 생성된 일시.
         * (NOTE: 필드명은 일반적으로 camelCase를 사용하지만, 여기서는 snake_case로 정의되어 있습니다.)
         */
        @JsonProperty("created_at") // JSON 필드명과 매핑
        String created_at,

        /**
         * 특정 조건(예: 아이디/이메일 중복 확인)의 존재 여부를 나타내는 플래그.
         * "Y" 또는 "N" 값을 가집니다.
         */
        @JsonProperty("exist_yn") // JSON 필드명과 매핑
        String exist_yn,

        /**
         * 전역 알림 활성화 여부.
         * (예: 예산 초과 알림 수신 여부) 등
         */
        Boolean globalAlertEnabled,

        /**
         * 물가 반영 예산 자동 조정 활성화 여부.
         */
        Boolean autoBudgetAdjustmentEnabled,

        /**
         * 사용자의 성별 (예: "M", "F").
         */
        String gender,

        /**
         * 사용자의 생년월일 (YYYY-MM-DD 형식).
         */
        String birthDate
) {
}