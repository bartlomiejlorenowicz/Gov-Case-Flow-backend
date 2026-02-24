package com.auditservice.tracing;

import org.slf4j.MDC;
import org.springframework.amqp.core.Message;

public final class MdcTrace {

    private static final String TRACE_ID_HEADER = "traceId";
    private static final String MDC_KEY = "traceId";

    private MdcTrace() {}

    public static void withTraceId(Message message, Runnable action) {
        String traceId = null;
        if (message != null && message.getMessageProperties() != null) {
            Object header = message.getMessageProperties().getHeaders().get(TRACE_ID_HEADER);
            if (header != null) traceId = header.toString();
        }

        if (traceId != null && !traceId.isBlank()) {
            MDC.put(MDC_KEY, traceId);
        }

        try {
            action.run();
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
