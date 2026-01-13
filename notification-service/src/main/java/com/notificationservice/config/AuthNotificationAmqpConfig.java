package com.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthNotificationAmqpConfig {

    public static final String EXCHANGE = "auth.events.exchange";
    public static final String ROUTING_KEY = "auth.user.registered";

    public static final String QUEUE = "notification.user-registered.queue";

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(userRegisteredQueue())
                .to(authExchange())
                .with(ROUTING_KEY);
    }
}