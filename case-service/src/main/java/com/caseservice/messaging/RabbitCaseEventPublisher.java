package com.caseservice.messaging;

import com.caseservice.configuration.CaseAmqpConfig;
import com.caseservice.event.CaseEventPublisher;
import com.caseservice.event.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitCaseEventPublisher implements CaseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishStatusChanged(CaseStatusChangedEvent event) {
        log.info("Publishing CaseStatusChangedEvent: {}", event);

        rabbitTemplate.convertAndSend(
                CaseAmqpConfig.EXCHANGE,
                CaseAmqpConfig.ROUTING_KEY,
                event
        );
    }
}
