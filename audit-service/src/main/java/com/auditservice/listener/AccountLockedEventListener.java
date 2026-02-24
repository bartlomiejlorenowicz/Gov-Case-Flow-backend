package com.auditservice.listener;

import com.auditservice.config.AuditAuthQueuesConfig;
import com.auditservice.tracing.MdcTrace;
import com.govcaseflow.events.auth.AccountLockedEvent;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLockedEventListener {

    private final AuditService auditService;

    @RabbitListener(queues = AuditAuthQueuesConfig.ACCOUNT_LOCKED_QUEUE)
    public void handle(AccountLockedEvent event, Message message) {
        MdcTrace.withTraceId(message, () -> {
            log.warn("Received AccountLockedEvent: {}", event);
            auditService.saveAccountLocked(event);
        });
    }
}
