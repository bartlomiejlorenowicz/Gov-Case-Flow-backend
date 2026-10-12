package com.govcaseflow.infrastructure.tracing;

import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;

/**
 * Copies the correlation ID from MDC into the AMQP "correlationId" message property.
 * An ID set explicitly by the caller is kept.
 */
public class CorrelationIdMessagePostProcessor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) {
        MessageProperties properties = message.getMessageProperties();

        if (properties.getCorrelationId() == null) {
            properties.setCorrelationId(CorrelationId.resolve(MDC.get(CorrelationId.MDC_KEY)));
        }

        return message;
    }
}
