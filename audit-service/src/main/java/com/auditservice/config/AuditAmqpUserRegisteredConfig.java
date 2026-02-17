package com.auditservice.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditAmqpUserRegisteredConfig {

    public static final String EXCHANGE = "auth.events.exchange";
    public static final String ROUTING_KEY = "auth.user.registered";
    public static final String QUEUE = "audit.user-registered.queue";

    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding userRegisteredBinding(
            @Qualifier("userRegisteredQueue") Queue queue,
            @Qualifier("authExchange") TopicExchange exchange
    ) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }
}
