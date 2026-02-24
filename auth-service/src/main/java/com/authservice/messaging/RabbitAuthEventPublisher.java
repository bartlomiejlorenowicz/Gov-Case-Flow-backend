package com.authservice.messaging;

import com.authservice.config.AuthAmqpConfig;
import com.authservice.event.AuthEventPublisher;
import com.govcaseflow.events.auth.AccountLockedEvent;
import com.govcaseflow.events.auth.UserPromotedEvent;
import com.govcaseflow.events.auth.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitAuthEventPublisher implements AuthEventPublisher {

    private static final String TRACE_ID_HEADER = "traceId";
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publishing UserRegisteredEvent: {}", event);
        send(AuthAmqpConfig.EXCHANGE, AuthAmqpConfig.ROUTING_KEY, event);
    }

    @Override
    public void publishUserPromoted(UserPromotedEvent event) {
        log.info("Publishing UserPromotedEvent: {}", event);
        send(AuthAmqpConfig.EXCHANGE, AuthAmqpConfig.USER_PROMOTED_ROUTING_KEY, event);
    }

    @Override
    public void publishAccountLocked(AccountLockedEvent event) {
        log.warn("Publishing AccountLockedEvent: {}", event);
        send(AuthAmqpConfig.EXCHANGE, AuthAmqpConfig.ACCOUNT_LOCKED_ROUTING_KEY, event);
    }

    private void send(String exchange, String routingKey, Object event) {
        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                event,
                message -> {
                    String traceId = MDC.get("traceId");
                    if (traceId != null && !traceId.isBlank()) {
                        message.getMessageProperties().setHeader(TRACE_ID_HEADER, traceId);
                    }
                    return message;
                }
        );
    }
}
