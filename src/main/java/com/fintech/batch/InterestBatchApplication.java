package com.fintech.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InterestBatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterestBatchApplication.class, args);
    }
}
