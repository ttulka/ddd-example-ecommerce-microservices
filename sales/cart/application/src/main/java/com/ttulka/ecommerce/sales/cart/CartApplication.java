package com.ttulka.ecommerce.sales.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Cart Spring Boot application.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAsync
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
