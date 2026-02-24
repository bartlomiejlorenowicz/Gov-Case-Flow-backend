package com.auditservice.listener;

import com.auditservice.config.AuditAuthQueuesConfig;
import com.auditservice.tracing.MdcTrace;
import com.govcaseflow.events.auth.UserRegisteredEvent;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final AuditService auditService;

    @RabbitListener(queues = AuditAuthQueuesConfig.USER_REGISTERED_QUEUE)
    public void handle(UserRegisteredEvent event, Message message) {
        MdcTrace.withTraceId(message, () -> {
            log.info("Received UserRegisteredEvent: {}", event);
            auditService.saveUserRegistered(event);
        });
    }
}