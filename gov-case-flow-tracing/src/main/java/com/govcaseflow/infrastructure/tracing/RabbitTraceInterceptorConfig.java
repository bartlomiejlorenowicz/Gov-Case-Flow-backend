package com.govcaseflow.infrastructure.tracing;

import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

public class RabbitTraceInterceptorConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory
    ) {
        SimpleRabbitListenerContainerFactory factory =
                new SimpleRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);

        factory.setAdviceChain((MethodInterceptor) invocation -> {

            Message message = null;
            for (Object arg : invocation.getArguments()) {
                if (arg instanceof Message msg) {
                    message = msg;
                    break;
                }
            }

            String traceId = extractTraceId(message);

            try {
                MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
                return invocation.proceed();
            } finally {
                MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
            }
        });

        return factory;
    }

    private String extractTraceId(Message message) {

        if (message == null) {
            return UUID.randomUUID().toString();
        }

        Object header = message.getMessageProperties()
                .getHeaders()
                .get(TraceConstants.TRACE_ID_HEADER);

        if (header instanceof String traceId) {
            try {
                UUID.fromString(traceId);
                return traceId;
            } catch (IllegalArgumentException ignored) {}
        }

        return UUID.randomUUID().toString();
    }
}
