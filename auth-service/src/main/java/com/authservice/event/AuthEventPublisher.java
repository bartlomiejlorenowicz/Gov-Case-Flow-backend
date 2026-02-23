package com.authservice.event;

public interface AuthEventPublisher {

    void publishUserRegistered(UserRegisteredEvent event);

    void publishUserPromoted(UserPromotedEvent event);

    void publishAccountLocked(AccountLockedEvent event);
}
