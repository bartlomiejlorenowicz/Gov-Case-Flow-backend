package com.govcaseflow.infrastructure.tracing;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;

/**
 * Listener container advice: puts the correlation ID of the consumed message into MDC
 * for the whole delivery (including retries) and clears it afterwards.
 */
public class CorrelationIdListenerAdvice implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MDC.put(CorrelationId.MDC_KEY, CorrelationId.resolve(extractCorrelationId(invocation.getArguments())));

        try {
            return invocation.proceed();
        } finally {
            MDC.remove(CorrelationId.MDC_KEY);
        }
    }

    private String extractCorrelationId(Object[] arguments) {
        for (Object argument : arguments) {
            if (argument instanceof Message message) {
                return message.getMessageProperties().getCorrelationId();
            }
        }
        return null;
    }
}
