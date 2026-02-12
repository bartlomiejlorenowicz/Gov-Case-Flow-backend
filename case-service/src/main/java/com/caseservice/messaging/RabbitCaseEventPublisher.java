package com.caseservice.messaging;

import com.caseservice.configuration.CaseAmqpConfig;
import com.caseservice.event.CaseEventPublisher;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitCaseEventPublisher implements CaseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishStatusChanged(CaseStatusChangedEvent event) {
        String traceId = MDC.get("traceId");
        log.info("Publishing CaseStatusChangedEvent: {} with traceId={}", event, traceId);

        rabbitTemplate.convertAndSend(
                CaseAmqpConfig.EXCHANGE,
                CaseAmqpConfig.ROUTING_KEY,
                event,
                message -> {
                    if (traceId != null) {
                        message.getMessageProperties().setHeader("traceId", traceId);
                    }
                    return message;
                }
        );
    }
}
