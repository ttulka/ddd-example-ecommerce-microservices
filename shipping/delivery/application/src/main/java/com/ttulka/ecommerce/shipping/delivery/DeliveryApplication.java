package com.ttulka.ecommerce.shipping.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Delivery Spring Boot application.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAsync
public class DeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryApplication.class, args);
    }
}
