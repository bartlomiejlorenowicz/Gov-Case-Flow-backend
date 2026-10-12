package com.notificationservice.config;

import com.govcaseflow.infrastructure.tracing.CorrelationIdListenerAdvice;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * notification-service does not component-scan com.govcaseflow, so correlation ID support
 * must come from the tracing module's auto-configuration. Listeners are not started,
 * so no broker is needed.
 */
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class CorrelationIdWiringTest {

    @Autowired
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory;

    @Test
    void listenerFactoryShouldPutCorrelationIdIntoMdc() {
        assertThat(rabbitListenerContainerFactory.getAdviceChain()[0])
                .isInstanceOf(CorrelationIdListenerAdvice.class);
    }
}
