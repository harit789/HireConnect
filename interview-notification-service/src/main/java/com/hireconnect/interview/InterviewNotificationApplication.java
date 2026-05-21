package com.hireconnect.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableCaching
public class InterviewNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewNotificationApplication.class, args);
    }
}
