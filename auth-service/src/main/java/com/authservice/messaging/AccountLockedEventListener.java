package com.authservice.messaging;

import com.authservice.event.AuthEventPublisher;
import com.govcaseflow.events.auth.AccountLockedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLockedEventListener {

    private final AuthEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AccountLockedEvent event) {
        log.warn("AFTER_COMMIT -> sending AccountLockedEvent: {}", event);
        publisher.publishAccountLocked(event);
    }
}
