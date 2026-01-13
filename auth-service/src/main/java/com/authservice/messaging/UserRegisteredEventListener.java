package com.authservice.messaging;


import com.authservice.event.AuthEventPublisher;
import com.authservice.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final AuthEventPublisher authEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserRegisteredEvent event) {
        log.info("AFTER_COMMIT -> sending UserRegisteredEvent to Rabbit: {}", event);
        authEventPublisher.publishUserRegistered(event);
    }
}