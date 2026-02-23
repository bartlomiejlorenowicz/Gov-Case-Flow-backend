package com.auditservice.listener;

import com.auditservice.config.AuditAmqpAuthConfig;
import com.auditservice.events.AccountLockedEvent;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLockedListener {

    private final AuditService auditService;

    @RabbitListener(queues = AuditAmqpAuthConfig.ACCOUNT_LOCKED_QUEUE)
    public void handle(AccountLockedEvent event) {
        log.warn("Received AccountLockedEvent: {}", event);
        auditService.saveAccountLocked(event);
    }
}
