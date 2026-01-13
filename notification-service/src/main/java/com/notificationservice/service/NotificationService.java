package com.notificationservice.service;

import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void notifyUser(CaseStatusChangedEvent event) {
        log.info("Sending notification: caseId={} status {} -> {}",
                event.caseId(), event.oldStatus(), event.newStatus());
    }
}