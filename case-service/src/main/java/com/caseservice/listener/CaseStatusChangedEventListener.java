package com.caseservice.listener;

import com.caseservice.messaging.RabbitCaseEventPublisher;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseStatusChangedEventListener {

    private final RabbitCaseEventPublisher rabbitPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CaseStatusChangedEvent event) {
        log.info("AFTER_COMMIT -> publish to Rabbit: {}", event);
        rabbitPublisher.publishStatusChanged(event);
    }
}