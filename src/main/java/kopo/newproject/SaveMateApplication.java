package kopo.newproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients
@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
@ComponentScan(basePackages = {"kopo.newproject", "kopo.newproject.aggregatedData"})
public class SaveMateApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaveMateApplication.class, args);
    }

}
