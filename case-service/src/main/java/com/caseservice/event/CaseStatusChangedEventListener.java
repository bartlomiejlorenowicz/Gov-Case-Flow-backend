//package com.caseservice.event;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class CaseStatusChangedEventListener {
//    private final CaseEventPublisher caseEventPublisher;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handle(CaseStatusChangedEvent event) {
//        log.info("Transaction committed. Publishing event to RabbitMQ: {}", event);
//        caseEventPublisher.publishStatusChanged(event);
//    }
//}
