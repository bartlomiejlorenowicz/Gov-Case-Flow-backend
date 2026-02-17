package com.auditservice.listener;

import com.auditservice.events.UserRegisteredEvent;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final AuditService auditService;

    @RabbitListener(queues = "audit.user-registered.queue")
    public void handle(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent: {}", event);
        auditService.saveUserRegistered(event);
    }
}