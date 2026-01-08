package com.caseservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaseAmqpConfig {

    public static final String EXCHANGE = "case.events.exchange";
    public static final String ROUTING_KEY = "case.status.changed";

    @Bean
    public TopicExchange caseExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }
}