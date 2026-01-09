package com.notificationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationAmqpConfig {

    public static final String EXCHANGE = "case.events.exchange";
    public static final String ROUTING_KEY = "case.status.changed";

    public static final String QUEUE = "notification.case-status.queue";

    @Bean
    public TopicExchange caseExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(caseExchange())
                .with(ROUTING_KEY);
    }
}
