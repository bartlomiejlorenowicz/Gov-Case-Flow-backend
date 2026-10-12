package com.govcaseflow.infrastructure.tracing;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CorrelationIdMessagingTest {

    private static final String ID = "3f2b8c1e-6d4a-4f7e-9b2c-1a2b3c4d5e6f";

    private final CorrelationIdMessagePostProcessor postProcessor = new CorrelationIdMessagePostProcessor();
    private final CorrelationIdListenerAdvice advice = new CorrelationIdListenerAdvice();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void publisherShouldCopyIdFromMdcToCorrelationIdProperty() {
        MDC.put(CorrelationId.MDC_KEY, ID);

        Message message = postProcessor.postProcessMessage(new Message(new byte[0]));

        assertThat(message.getMessageProperties().getCorrelationId()).isEqualTo(ID);
    }

    @Test
    void publisherShouldGenerateIdWhenMdcEmpty() {
        Message message = postProcessor.postProcessMessage(new Message(new byte[0]));

        assertThat(CorrelationId.isValid(message.getMessageProperties().getCorrelationId())).isTrue();
    }

    @Test
    void publisherShouldKeepExplicitCorrelationId() {
        MDC.put(CorrelationId.MDC_KEY, ID);
        Message message = messageWithCorrelationId("explicit");

        postProcessor.postProcessMessage(message);

        assertThat(message.getMessageProperties().getCorrelationId()).isEqualTo("explicit");
    }

    @Test
    void listenerShouldExposeMessageIdInMdcAndClearAfterwards() throws Throwable {
        AtomicReference<String> idInListener = new AtomicReference<>();

        advice.invoke(invocation(messageWithCorrelationId(ID),
                () -> idInListener.set(MDC.get(CorrelationId.MDC_KEY))));

        assertThat(idInListener.get()).isEqualTo(ID);
        assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
    }

    @Test
    void listenerShouldGenerateIdWhenMessageHasNone() throws Throwable {
        AtomicReference<String> idInListener = new AtomicReference<>();

        advice.invoke(invocation(new Message(new byte[0]),
                () -> idInListener.set(MDC.get(CorrelationId.MDC_KEY))));

        assertThat(CorrelationId.isValid(idInListener.get())).isTrue();
    }

    @Test
    void listenerShouldClearMdcWhenListenerThrows() throws Throwable {
        MethodInvocation failing = invocation(messageWithCorrelationId(ID), () -> {
            throw new IllegalStateException("boom");
        });

        assertThatThrownBy(() -> advice.invoke(failing)).isInstanceOf(IllegalStateException.class);
        assertThat(MDC.get(CorrelationId.MDC_KEY)).isNull();
    }

    private static Message messageWithCorrelationId(String correlationId) {
        MessageProperties properties = new MessageProperties();
        properties.setCorrelationId(correlationId);
        return new Message(new byte[0], properties);
    }

    private static MethodInvocation invocation(Message message, Runnable listener) throws Throwable {
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getArguments()).thenReturn(new Object[] {"channel", message});
        when(invocation.proceed()).thenAnswer(call -> {
            listener.run();
            return null;
        });
        return invocation;
    }
}
