package com.ttulka.ecommerce.infra;

import com.ttulka.ecommerce.common.events.DomainEvent;
import com.ttulka.ecommerce.common.events.EventPublisher;
import lombok.RequiredArgsConstructor;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Redis is used as a message broker.
 * It brings its own implementation of {@code EventPublisher} to send events to the Redis server.
 * Received message are then re-sent as Spring application events.
 */
@Profile("redis")
@Configuration
public class RedisConfig {

    @Primary
    @Bean
    EventPublisher redisEventPublisher(RedisTemplate<String, DomainEvent> redisTemplate, Topic topic) {
        return evt -> redisTemplate.convertAndSend(topic.getTopic(), evt);
    }

    @Bean
    ChannelTopic channelTopic() {
        return new ChannelTopic("ecommerce");
    }

    @Bean
    RedisMessageListenerContainer redisContainer(RedisConnectionFactory factory, MessageListenerAdapter adapter, Topic topic) {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(new MessageListenerAdapter(adapter), topic);
        return container;
    }

    @Bean
    MessageListenerAdapter redisMessageAdapter(EventListenerApplicationAdapter eventListenerApplicationAdapter) {
        return new MessageListenerAdapter(eventListenerApplicationAdapter);
    }

    @Bean
        // it is important for this instance to be declared as a bean for @Transactional to take effect
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

    // listens to Redis, re-publishes as Spring application events
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

    @Configuration
    static class SerializationConfig {

        @Bean
        JdkSerializationRedisSerializer jdkSerializationRedisSerializer() {
            return new JdkSerializationRedisSerializer();
        }
    }
}
