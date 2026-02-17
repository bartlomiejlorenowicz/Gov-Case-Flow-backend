package com.auditservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditAmqpCaseStatusConfig {

    public static final String EXCHANGE = "case.events.exchange";
    public static final String QUEUE = "audit.case-status.queue";
    public static final String ROUTING_KEY = "case.status.changed";

    @Bean
    public TopicExchange caseExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder
                .bind(auditQueue())
                .to(caseExchange())
                .with(ROUTING_KEY);
    }
}