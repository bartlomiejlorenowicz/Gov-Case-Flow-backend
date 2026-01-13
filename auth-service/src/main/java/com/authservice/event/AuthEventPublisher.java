package com.authservice.event;

public interface AuthEventPublisher {

    void publishUserRegistered(UserRegisteredEvent event);
}
