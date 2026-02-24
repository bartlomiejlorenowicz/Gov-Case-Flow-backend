package com.auditservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditAuthQueuesConfig {

    // Queues
    public static final String USER_REGISTERED_QUEUE = "audit.user-registered.queue";
    public static final String USER_PROMOTED_QUEUE = "audit.user-promoted.queue";
    public static final String ACCOUNT_LOCKED_QUEUE = "audit.account-locked.queue";

    // Routing keys
    public static final String USER_REGISTERED_ROUTING_KEY = "auth.user.registered";
    public static final String USER_PROMOTED_ROUTING_KEY = "auth.user.promoted";
    public static final String ACCOUNT_LOCKED_ROUTING_KEY = "auth.account.locked";

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(USER_REGISTERED_QUEUE).build();
    }

    @Bean
    public Queue userPromotedQueue() {
        return QueueBuilder.durable(USER_PROMOTED_QUEUE).build();
    }

    @Bean
    public Queue accountLockedQueue() {
        return QueueBuilder.durable(ACCOUNT_LOCKED_QUEUE).build();
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userRegisteredQueue)
                .to(authExchange)
                .with(USER_REGISTERED_ROUTING_KEY);
    }

    @Bean
    public Binding userPromotedBinding(Queue userPromotedQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(userPromotedQueue)
                .to(authExchange)
                .with(USER_PROMOTED_ROUTING_KEY);
    }

    @Bean
    public Binding accountLockedBinding(Queue accountLockedQueue, TopicExchange authExchange) {
        return BindingBuilder.bind(accountLockedQueue)
                .to(authExchange)
                .with(ACCOUNT_LOCKED_ROUTING_KEY);
    }
}
