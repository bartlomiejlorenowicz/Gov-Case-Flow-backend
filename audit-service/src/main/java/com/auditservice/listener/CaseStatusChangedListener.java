package com.auditservice.listener;

import com.auditservice.config.AuditAmqpCaseStatusConfig;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseStatusChangedListener {

    private final AuditService auditService;

    @RabbitListener(queues = AuditAmqpCaseStatusConfig.QUEUE)
    public void handle(CaseStatusChangedEvent event) {
        log.info("Received CaseStatusChangedEvent: {}", event);
        auditService.save(event);
    }
}