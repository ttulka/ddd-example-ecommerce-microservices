package com.ttulka.ecommerce.common.integration.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ttulka.ecommerce.common.events.DomainEvent;
import com.ttulka.ecommerce.common.events.EventPublisher;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RabbitMq is used as a message broker. It brings its own implementation of {@code EventPublisher} to send events to the RabbitMq broker.
 * Received message are then re-sent as Spring application events.
 * <p>
 * [Domain Publisher] -(DomainEvent)⟶ [EventPublisher] -(DomainEventWrapper)⟶ {Spring AppCtx}
 * ⟶ [ApplicationEventsListenerAdapter] -(DomainEvent)⟶ {Rabbit TopicExchange(DomainEvent.className)}
 * ⟶ [EventListenerApplicationAdapter] -(DomainEvent)⟶ {Spring AppCtx} ⟶ [Domain Listeners]
 */
@Profile("rabbitmq")
@Configuration
public class RabbitMqConfig {

    private static final String TOPIC_EXCHANGE_NAME = "application-exchange";
    private static final String QUEUE_NAME = "service-queue";
    private static final String ROUTING_KEY = "domain.events";

    @Primary
    @Bean
    EventPublisher rabbitmqEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return evt -> applicationEventPublisher.publishEvent(new DomainEventWrapper(evt));
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME);
    }

    @Bean
    Queue queue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, EventListenerApplicationAdapter listener) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QUEUE_NAME);
        container.setMessageListener(listener);
        return container;
    }

    @Bean
    public MessageConverter jsonConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    // listens to Spring application events, re-publishes as RabbitMq messages
    // this is important to make the events work in transactional manner
    @Component
    @RequiredArgsConstructor
    static class ApplicationEventsListenerAdapter {

        private final RabbitTemplate rabbitTemplate;

        @TransactionalEventListener // only committed events are sent
        @Async
        public void on(DomainEventWrapper wrappedEvent) {
            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_NAME, ROUTING_KEY, wrappedEvent.getEvent());
        }
    }

    // listens to RabbitMq messages, re-publishes as Spring application events
    @Component
    @RequiredArgsConstructor
    static class EventListenerApplicationAdapter implements MessageListener {

        private final ApplicationEventPublisher publisher;
        private final MessageConverter messageConverter;

        @Transactional // important due to transactional listeners
        @Override
        public void onMessage(Message message) {
            publisher.publishEvent(messageConverter.fromMessage(message));
        }
    }

    // wraps the original domain event to be sent via Spring application events and re-sent via RabbitMq afterwards
    @Getter
    @RequiredArgsConstructor
    private static class DomainEventWrapper {

        private final DomainEvent event;
    }
}
