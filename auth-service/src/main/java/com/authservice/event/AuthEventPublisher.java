package com.authservice.event;

import com.govcaseflow.events.auth.AccountLockedEvent;
import com.govcaseflow.events.auth.UserPromotedEvent;
import com.govcaseflow.events.auth.UserRegisteredEvent;

public interface AuthEventPublisher {

    void publishUserRegistered(UserRegisteredEvent event);

    void publishUserPromoted(UserPromotedEvent event);

    void publishAccountLocked(AccountLockedEvent event);
}
