package kopo.newproject;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SaveMate 어플리케이션의 메인 실행 클래스.
 * <p>
 * {@code @SpringBootApplication} - 스프링 부트의 자동 설정, 빈 생성 등을 활성화합니다.
 * {@code @EnableFeignClients} - Feign 클라이언트(선언적 REST 클라이언트)를 활성화합니다.
 * {@code @EnableMongoAuditing} - MongoDB의 Auditing 기능을 활성화하여 생성/수정 시간을 자동으로 기록합니다.
 * {@code @EnableScheduling} - 스케줄링 기능을 활성화하여 주기적인 작업 실행을 지원합니다.
 * {@code @ComponentScan} - 특정 패키지(기본 패키지 및 aggregatedData)를 스캔하여 빈으로 등록합니다.
 */
@Slf4j
@EnableFeignClients
@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
@ComponentScan(basePackages = {"kopo.newproject", "kopo.newproject.aggregatedData"})
public class SaveMateApplication {

    /**
     * 어플리케이션의 주 진입점(Entry Point).
     * SpringApplication.run()을 호출하여 내장 WAS(Tomcat)를 실행하고 스프링 부트 어플리케이션을 시작합니다.
     *
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        log.info("SaveMate Application을 시작합니다.");
        SpringApplication.run(SaveMateApplication.class, args);
        log.info("SaveMate Application이 성공적으로 시작되었습니다.");
    }

}
