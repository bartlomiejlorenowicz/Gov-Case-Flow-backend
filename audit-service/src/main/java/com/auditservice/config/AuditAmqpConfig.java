package com.auditservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditAmqpConfig {

    public static final String CASE_EXCHANGE = "case.exchange";
    public static final String AUDIT_QUEUE = "audit.case-status.queue";
    public static final String ROUTING_KEY_PATTERN = "case.status.*";

    @Bean
    public Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true);
    }

    @Bean
    public TopicExchange caseExchange() {
        return new TopicExchange(CASE_EXCHANGE);
    }

    @Bean
    public Binding auditBinding(
            Queue auditQueue,
            TopicExchange caseExchange
    ) {
        return BindingBuilder
                .bind(auditQueue)
                .to(caseExchange)
                .with(ROUTING_KEY_PATTERN);
    }
}
