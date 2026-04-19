package com.pixelbase.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BackendPixelbaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendPixelbaseApplication.class, args);
    }

}
