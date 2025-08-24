package kopo.newproject.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 애플리케이션 전반에 사용될 공통 Bean(객체)들을 정의하는 설정 클래스.
 * {@code @Configuration} - 이 클래스가 Spring의 설정 정보를 담고 있음을 나타냅니다.
 * Spring 컨테이너는 이 클래스를 스캔하여 {@code @Bean} 어노테이션이 붙은 메소드들을 실행하고,
 * 그 반환값들을 Bean으로 등록합니다.
 */
@Slf4j
@Configuration
public class AppConfig {

    /**
     * 외부 REST API와 통신하기 위한 RestTemplate 객체를 Spring Bean으로 등록합니다.
     * RestTemplate은 동기(synchronous) 방식으로 HTTP 요청을 보내고 응답을 받는데 사용되는
     * 스프링의 전통적인 HTTP 통신 도구입니다.
     *
     * @return Spring 컨테이너가 관리할 RestTemplate의 새로운 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        log.info("RestTemplate Bean을 생성하여 등록합니다.");
        return new RestTemplate();
    }
}
