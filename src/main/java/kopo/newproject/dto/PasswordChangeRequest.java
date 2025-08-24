package kopo.newproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 비밀번호 변경 요청에 필요한 정보를 담는 DTO(Data Transfer Object).
 * <p>
 * 클라이언트로부터 현재 비밀번호와 새로운 비밀번호를 받아 서비스 계층으로 전달하는 데 사용됩니다.
 * <p>
 * {@code @Data} - Lombok 어노테이션으로, {@code @Getter}, {@code @Setter}, {@code @ToString}, {@code @EqualsAndHashCode}, {@code @RequiredArgsConstructor}를 포함합니다.
 * {@code @Builder} - Lombok 어노테이션으로, 빌더 패턴을 사용하여 객체를 생성할 수 있도록 합니다.
 * {@code @NoArgsConstructor} - Lombok 어노테이션으로, 인자 없는 기본 생성자를 자동으로 생성합니다.
 * {@code @AllArgsConstructor} - Lombok 어노테이션으로, 모든 필드를 인자로 받는 생성자를 자동으로 생성합니다.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PasswordChangeRequest {

    /**
     * 사용자의 현재 비밀번호.
     * 새로운 비밀번호로 변경하기 전, 본인 확인을 위해 필요합니다.
     */
    private String currentPassword;

    /**
     * 사용자가 새로 설정하고자 하는 비밀번호.
     * 이 필드는 서비스 계층에서 암호화되어 저장되어야 합니다.
     */
    private String newPassword;
}