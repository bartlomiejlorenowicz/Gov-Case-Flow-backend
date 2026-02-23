package com.auditservice.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditAmqpUserPromotedConfig {

    public static final String EXCHANGE = "auth.events.exchange";
    public static final String USER_PROMOTED_ROUTING_KEY = "auth.user.promoted";
    public static final String QUEUE = "audit.user-promoted.queue";

    @Bean
    public Queue userPromotedQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding userPromotedBinding(
            @Qualifier("userPromotedQueue") Queue queue,
            @Qualifier("authExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(USER_PROMOTED_ROUTING_KEY);
    }
}
