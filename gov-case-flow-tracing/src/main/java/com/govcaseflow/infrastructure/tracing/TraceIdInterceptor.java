package com.govcaseflow.infrastructure.tracing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

public class TraceIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String traceId = extractOrGenerateTraceId(request);

        MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
        response.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
    }

    private String extractOrGenerateTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(TraceConstants.TRACE_ID_HEADER);

        if (incoming != null) {
            try {
                UUID.fromString(incoming);
                return incoming;
            } catch (IllegalArgumentException ignored) {}
        }

        return UUID.randomUUID().toString();
    }
}