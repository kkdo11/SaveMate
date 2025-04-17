package kopo.newproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class NewProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewProjectApplication.class, args);
    }

}
