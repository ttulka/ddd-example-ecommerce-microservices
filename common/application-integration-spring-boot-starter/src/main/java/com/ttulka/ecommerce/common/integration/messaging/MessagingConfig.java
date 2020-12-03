package com.ttulka.ecommerce.common.integration.messaging;

import com.ttulka.ecommerce.common.events.EventPublisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Configuration for messaging.
 */
@Configuration
@Import({RedisBrokerConfig.class, RabbitMqConfig.class})
public class MessagingConfig {

    /**
     * Default message broker implemented by Spring application events.
     */
    @Configuration
    static class SpringMessagingConfig {

        @Bean
        EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            return applicationEventPublisher::publishEvent;
        }
    }
}
