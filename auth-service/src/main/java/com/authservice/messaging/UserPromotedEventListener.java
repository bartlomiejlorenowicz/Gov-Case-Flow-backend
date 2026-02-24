package com.authservice.messaging;

import com.authservice.event.AuthEventPublisher;
import com.govcaseflow.events.auth.UserPromotedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPromotedEventListener {

    private final AuthEventPublisher authEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserPromotedEvent event) {
        try {
            authEventPublisher.publishUserPromoted(event);
        } catch (Exception e) {
            log.error("Failed to publish UserPromotedEvent", e);
        }
    }
}
