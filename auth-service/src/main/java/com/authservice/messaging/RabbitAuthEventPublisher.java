package com.authservice.messaging;

import com.authservice.config.AuthAmqpConfig;
import com.authservice.event.AuthEventPublisher;
import com.authservice.event.UserPromotedEvent;
import com.authservice.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitAuthEventPublisher implements AuthEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        log.info("Publishing UserRegisteredEvent: {}", event);

        rabbitTemplate.convertAndSend(
                AuthAmqpConfig.EXCHANGE,
                AuthAmqpConfig.ROUTING_KEY,
                event
        );
    }

    @Override
    public void publishUserPromoted(UserPromotedEvent event) {
        log.info("Publishing UserPromotedEvent: {}", event);

        rabbitTemplate.convertAndSend(
                AuthAmqpConfig.EXCHANGE,
                AuthAmqpConfig.USER_PROMOTED_ROUTING_KEY,
                event
        );
    }
}
