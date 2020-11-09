package com.ttulka.ecommerce.common.integration;

import com.ttulka.ecommerce.common.events.DomainEvent;
import com.ttulka.ecommerce.common.events.EventPublisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Configuration for messaging.
 */
@Configuration
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

    /**
     * Redis is used as a message broker.
     * It brings its own implementation of {@code EventPublisher} to send events to the Redis server.
     * Received message are then re-sent as Spring application events.
     */
    @Profile("redis")
    @Configuration
    static class RedisMessagingConfig {

        @Primary
        @Bean
        EventPublisher redisEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            return evt -> applicationEventPublisher.publishEvent(new DomainEventWrapper(evt));
        }

        @Bean
        ChannelTopic applicationTopic() {
            return new ChannelTopic("ecommerce");
        }

        @Bean
        RedisMessageListenerContainer redisContainer(RedisConnectionFactory factory, MessageListenerAdapter adapter, Topic applicationTopic) {
            var container = new RedisMessageListenerContainer();
            container.setConnectionFactory(factory);
            container.addMessageListener(new MessageListenerAdapter(adapter), applicationTopic);
            return container;
        }

        @Bean
        MessageListenerAdapter redisMessageAdapter(EventListenerApplicationAdapter eventListenerApplicationAdapter) {
            return new MessageListenerAdapter(eventListenerApplicationAdapter);
        }

        @Bean
            // it is important for this instance to be declared as a bean for @Transactional in {@code EventListenerApplicationAdapter} to take effect
        EventListenerApplicationAdapter eventListenerApplicationAdapter(RedisSerializer redisSerializer, ApplicationEventPublisher applicationEventPublisher) {
            return new EventListenerApplicationAdapter(redisSerializer, applicationEventPublisher);
        }

        @Bean
        RedisTemplate<String, DomainEvent> redisTemplate(RedisConnectionFactory factory, RedisSerializer redisSerializer) {
            var template = new RedisTemplate<String, DomainEvent>();
            template.setConnectionFactory(factory);
            template.setDefaultSerializer(redisSerializer);
            return template;
        }

        // listens to Redis messages, re-publishes as Spring application events
        @RequiredArgsConstructor
        static class EventListenerApplicationAdapter implements MessageListener {

            private final RedisSerializer serializer;
            private final ApplicationEventPublisher publisher;

            @Transactional // important due to transactional listeners
            @Override
            public void onMessage(Message message, byte[] pattern) {
                publisher.publishEvent(serializer.deserialize(message.getBody()));
            }
        }

        // listens to Spring application events, re-publishes as Redis messages
        // this is important to make the events work in transactional manner
        @Component
        @RequiredArgsConstructor
        static class ApplicationEventsListenerAdapter {

            private final RedisTemplate<String, DomainEvent> redisTemplate;
            private final Topic applicationTopic;

            @TransactionalEventListener // only committed events are sent
            @Async
            public void on(DomainEventWrapper wrappedEvent) {
                redisTemplate.convertAndSend(applicationTopic.getTopic(), wrappedEvent.getEvent());
            }
        }

        @Configuration
        static class SerializationConfig {

            @Bean
            JdkSerializationRedisSerializer jdkSerializationRedisSerializer() {
                return new JdkSerializationRedisSerializer();
            }
        }

        // wraps the original domain event to be sent via Spring application events and re-sent via Redis afterwards
        @Getter
        @RequiredArgsConstructor
        private static class DomainEventWrapper {

            private final DomainEvent event;
        }
    }
}
