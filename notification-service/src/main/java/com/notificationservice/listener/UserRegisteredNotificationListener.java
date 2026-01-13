package com.notificationservice.listener;

import com.notificationservice.config.AuthNotificationAmqpConfig;
import com.notificationservice.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredNotificationListener {

    @RabbitListener(queues = AuthNotificationAmqpConfig.QUEUE)
    public void handle(UserRegisteredEvent event) {
        log.info("NOTIFICATION: New user registered: {} (id={}) at {}",
                event.email(), event.userId(), event.registeredAt());
    }
}