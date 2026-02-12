package com.notificationservice.listener;

import com.notificationservice.config.NotificationAmqpConfig;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class CaseStatusChangedNotificationListener {

    @RabbitListener(queues = NotificationAmqpConfig.QUEUE)
    public void handle(CaseStatusChangedEvent event) {
        log.info("NOTIFICATION: Case {} changed status {} -> {} by {} at {}",
                event.caseId(),
                event.oldStatus(),
                event.newStatus(),
                event.changedBy(),
                event.changedAt()
        );
    }
}
