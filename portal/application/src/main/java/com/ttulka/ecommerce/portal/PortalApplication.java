package com.ttulka.ecommerce.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Portal Spring Boot application.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAsync
public class PortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
