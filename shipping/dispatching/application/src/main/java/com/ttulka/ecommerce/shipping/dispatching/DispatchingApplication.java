package com.ttulka.ecommerce.shipping.dispatching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Dispatching Spring Boot application.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAsync
public class DispatchingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DispatchingApplication.class, args);
    }
}
