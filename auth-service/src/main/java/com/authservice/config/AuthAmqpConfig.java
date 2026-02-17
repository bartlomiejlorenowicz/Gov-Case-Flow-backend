package com.authservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthAmqpConfig {

    public static final String EXCHANGE = "auth.events.exchange";
    public static final String ROUTING_KEY = "auth.user.registered";
    public static final String USER_PROMOTED_ROUTING_KEY = "auth.user.promoted";

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2Converter() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();

        return new Jackson2JsonMessageConverter(mapper);
    }
}
