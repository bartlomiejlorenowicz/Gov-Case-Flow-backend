package com.auditservice.listener;

import com.auditservice.events.UserPromotedEvent;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPromotedEventListener {

    private final AuditService auditService;

    @RabbitListener(queues = "audit.user-promoted.queue")
    public void handle(UserPromotedEvent event) {
        log.info("Received UserPromotedEvent: {}", event);
        auditService.saveUserPromoted(event);
    }
}