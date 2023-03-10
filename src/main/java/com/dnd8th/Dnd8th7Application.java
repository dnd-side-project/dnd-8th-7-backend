package com.dnd8th;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Dnd8th7Application {

    public static void main(String[] args) {
        SpringApplication.run(Dnd8th7Application.class, args);
    }

}
