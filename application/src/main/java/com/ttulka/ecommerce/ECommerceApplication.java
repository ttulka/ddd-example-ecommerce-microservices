package com.ttulka.ecommerce;

import com.ttulka.ecommerce.common.events.EventPublisher;
import com.ttulka.ecommerce.infra.RedisMessagingConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot based monolithic application.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAsync
@Import({RedisMessagingConfig.class})
public class ECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }

    /**
     * Default message broker implemented by Spring application events.
     */
    @Bean
    EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }
}
