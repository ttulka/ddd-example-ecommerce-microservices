package com.ttulka.ecommerce.common.integration;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ttulka.ecommerce.common.events.DomainEvent;
import com.ttulka.ecommerce.common.events.EventPublisher;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import static java.util.stream.Collectors.toSet;

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
     * Redis is used as a message broker. It brings its own implementation of {@code EventPublisher} to send events to the Redis server. Received message are
     * then re-sent as Spring application events.
     *
     * [Domain Publisher] -(DomainEvent)-> [EventPublisher] -(DomainEventWrapper)-> {Spring AppCtx}
     * --> [ApplicationEventsListenerAdapter] -(DomainEvent)-> {Redis Topic(DomainEvent.className)}
     * --> [EventListenerApplicationAdapter] -(DomainEvent)-> {Spring AppCtx}
     * --> [Domain Listeners]
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
        RedisMessageListenerContainer redisContainer(
                RedisConnectionFactory factory, MessageListenerAdapter adapter,
                ApplicationContext context, ConfigurableListableBeanFactory beanFactory) {
            var container = new RedisMessageListenerContainer();
            container.setConnectionFactory(factory);

            // find all Spring event listeners and register Redis listeners only for those events:
            var eventClasses = new SubscribedEvents(context, beanFactory).classes();
            if (!eventClasses.isEmpty()) {
                container.addMessageListener(adapter, eventClasses.stream()
                        .map(clazz -> topicNameFrom(clazz))
                        .map(ChannelTopic::new).collect(toSet()));
            }
            return container;
        }

        @Bean
        MessageListenerAdapter redisMessageAdapter(EventListenerApplicationAdapter eventListenerApplicationAdapter) {
            return new MessageListenerAdapter(eventListenerApplicationAdapter);
        }

        @Bean
            // it is important for this instance to be declared as a bean for @Transactional in {@code EventListenerApplicationAdapter} to take effect
        EventListenerApplicationAdapter eventListenerApplicationAdapter(GenericJackson2JsonRedisSerializer redisSerializer, ApplicationEventPublisher applicationEventPublisher) {
            return new EventListenerApplicationAdapter(redisSerializer, applicationEventPublisher);
        }

        @Bean
        RedisTemplate<String, DomainEvent> redisTemplate(RedisConnectionFactory factory, RedisSerializer redisSerializer) {
            var template = new RedisTemplate<String, DomainEvent>();
            template.setConnectionFactory(factory);
            template.setDefaultSerializer(redisSerializer);
            return template;
        }

        @Bean
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return new GenericJackson2JsonRedisSerializer(mapper);
        }

        static String topicNameFrom(Class<?> eventClass) {
            return eventClass.getCanonicalName();
        }

        // listens to Redis messages, re-publishes as Spring application events
        @RequiredArgsConstructor
        static class EventListenerApplicationAdapter implements MessageListener {

            private final GenericJackson2JsonRedisSerializer serializer;
            private final ApplicationEventPublisher publisher;

            @Transactional // important due to transactional listeners
            @SneakyThrows
            @Override
            public void onMessage(Message message, byte[] pattern) {
                publisher.publishEvent(serializer.deserialize(message.getBody(), Class.forName(new String(message.getChannel()))));
            }
        }

        // listens to Spring application events, re-publishes as Redis messages
        // this is important to make the events work in transactional manner
        @Component
        @RequiredArgsConstructor
        static class ApplicationEventsListenerAdapter {

            private final RedisTemplate<String, DomainEvent> redisTemplate;

            @TransactionalEventListener // only committed events are sent
            @Async
            public void on(DomainEventWrapper wrappedEvent) {
                DomainEvent event = wrappedEvent.getEvent();
                redisTemplate.convertAndSend(topicNameFrom(event.getClass()), event);
            }
        }

        // wraps the original domain event to be sent via Spring application events and re-sent via Redis afterwards
        @Getter
        @RequiredArgsConstructor
        private static class DomainEventWrapper {

            private final DomainEvent event;
        }
    }

    /**
     * Finds the {@code EventListener}-annotated methods that listens to {@code DomainEvent} events.
     */
    @RequiredArgsConstructor
    static class SubscribedEvents {

        private final @NonNull ApplicationContext context;
        private final @NonNull ConfigurableListableBeanFactory beanFactory;

        public Set<Class<?>> classes() {
            return classesAnnotatedWithComponent()
                    .flatMap(this::methodsAnnotatedWithEventListener)
                    .flatMap(this::eventClassesFromMethodParameters)
                    .collect(toSet());
        }

        private Stream<? extends Class<?>> classesAnnotatedWithComponent() {
            return Stream.of(context.getBeanNamesForAnnotation(Component.class))
                    .map(bean -> AutoProxyUtils.determineTargetClass(beanFactory, bean))
                    .filter(c -> !isSpringContainerClass(c));
        }

        private Stream<Method> methodsAnnotatedWithEventListener(Class<?> clazz) {
            return MethodIntrospector.selectMethods(
                    clazz,
                    (MethodIntrospector.MetadataLookup<EventListener>) method ->
                            AnnotatedElementUtils.findMergedAnnotation(method, EventListener.class))
                    .keySet().stream();
        }

        private Stream<Class<?>> eventClassesFromMethodParameters(Method method) {
            return Stream.of(method.getParameterTypes())
                    .filter(DomainEvent.class::isAssignableFrom);
        }

        private boolean isSpringContainerClass(Class<?> clazz) {
            return clazz.getName().startsWith("org.springframework.");
        }
    }
}
