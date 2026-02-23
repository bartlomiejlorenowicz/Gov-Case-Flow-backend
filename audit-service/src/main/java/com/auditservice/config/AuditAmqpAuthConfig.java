package com.auditservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditAmqpAuthConfig {

    public static final String EXCHANGE = "auth.events.exchange";
    public static final String ACCOUNT_LOCKED_QUEUE = "audit.account-locked.queue";
    public static final String ACCOUNT_LOCKED_ROUTING_KEY = "auth.account.locked";

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue accountLockedQueue() {
        return QueueBuilder.durable(ACCOUNT_LOCKED_QUEUE).build();
    }

    @Bean
    public Binding accountLockedBinding() {
        return BindingBuilder
                .bind(accountLockedQueue())
                .to(authExchange())
                .with(ACCOUNT_LOCKED_ROUTING_KEY);
    }
}
