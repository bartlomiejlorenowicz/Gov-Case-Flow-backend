package com.caseservice.messaging;

import com.caseservice.configuration.CaseAmqpConfig;
import com.caseservice.event.CaseEventPublisher;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import com.govcaseflow.infrastructure.tracing.TraceConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitCaseEventPublisher implements CaseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishStatusChanged(CaseStatusChangedEvent event) {
        String traceId = MDC.get(TraceConstants.TRACE_ID_MDC_KEY);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        log.info("Publishing CaseStatusChangedEvent: {} with traceId={}", event, traceId);

        String finalTraceId = traceId;

        rabbitTemplate.convertAndSend(
                CaseAmqpConfig.EXCHANGE,
                CaseAmqpConfig.ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setMessageId(UUID.randomUUID().toString());

                    message.getMessageProperties()
                            .setHeader(TraceConstants.TRACE_ID_HEADER, finalTraceId);

                    return message;
                }
        );
    }
}
