package com.notificationservice.listener;

import com.notificationservice.config.NotificationAmqpConfig;
import com.notificationservice.event.CaseStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
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
