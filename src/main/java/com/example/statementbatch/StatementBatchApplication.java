package com.example.statementbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StatementBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatementBatchApplication.class, args);
    }
}
