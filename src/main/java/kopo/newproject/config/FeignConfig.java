package kopo.newproject.config;

import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Cloud OpenFeign 클라이언트의 전역 설정을 담당하는 클래스.
 * <p>
 * {@code @Configuration} - 이 클래스가 Spring의 설정 정보를 담고 있음을 나타냅니다.
 * <p>
 * 이 클래스에 Bean들을 정의하여 모든 Feign 클라이언트(@FeignClient)의 동작을 상세하게 제어할 수 있습니다.
 * <p>
 * 설정 가능한 주요 Bean 예시:
 * - {@link feign.Logger.Level}: Feign 클라이언트의 로깅 레벨을 설정 (NONE, BASIC, HEADERS, FULL)
 * - {@link feign.codec.ErrorDecoder}: API 호출 시 발생하는 오류(4xx, 5xx)에 대한 커스텀 처리 로직 정의
 * - {@link feign.RequestInterceptor}: 모든 API 요청에 공통 헤더(예: 인증 토큰)를 추가
 * - {@link feign.codec.Encoder}, {@link feign.codec.Decoder}: 요청/응답을 직렬화/역직렬화하는 방식을 커스터마이징
 */
@Slf4j
@Configuration
public class FeignConfig {

    public FeignConfig() {
        log.info("Feign 전역 설정 클래스가 초기화되었습니다.");
    }

    /**
     * Feign 클라이언트의 로깅 레벨을 설정하는 Bean.
     * FULL 레벨은 요청/응답의 헤더, 바디, 메타데이터 등 모든 정보를 로깅하므로,
     * 개발 및 디버깅 시 매우 유용합니다. 프로덕션 환경에서는 성능을 고려하여 BASIC이나 NONE으로 변경하는 것을 권장합니다.
     *
     * @return 로거 레벨
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        log.info("Feign Logger Level을 FULL로 설정합니다.");
        return Logger.Level.FULL;
    }
}
