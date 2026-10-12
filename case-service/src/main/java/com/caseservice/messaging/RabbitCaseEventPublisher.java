package com.caseservice.messaging;

import com.caseservice.configuration.CaseAmqpConfig;
import com.caseservice.event.CaseEventPublisher;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitCaseEventPublisher implements CaseEventPublisher {

    private final @Qualifier("caseRabbitTemplate") RabbitTemplate rabbitTemplate;

    @Override
    public void publishStatusChanged(CaseStatusChangedEvent event) {
        log.info("Publishing CaseStatusChangedEvent: {}", event);

        // correlationId is set by CorrelationIdMessagePostProcessor on caseRabbitTemplate
        rabbitTemplate.convertAndSend(
                CaseAmqpConfig.EXCHANGE,
                CaseAmqpConfig.STATUS_CHANGED_KEY,
                event,
                message -> {
                    message.getMessageProperties()
                            .setMessageId(UUID.randomUUID().toString());
                    return message;
                }
        );
    }
}
