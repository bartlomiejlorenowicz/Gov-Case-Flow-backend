package com.auditservice.listener;

import com.auditservice.config.AuditAmqpConfig;
import com.auditservice.event.CaseStatusChangedEvent;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaseStatusChangedListener {

    private final AuditService auditService;

    @RabbitListener(queues = AuditAmqpConfig.AUDIT_QUEUE)
    public void handle(CaseStatusChangedEvent event) {
        auditService.save(event);
    }
}