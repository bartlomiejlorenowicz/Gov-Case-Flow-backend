package com.govcaseflow.infrastructure.tracing;

import java.util.UUID;
import java.util.regex.Pattern;

public final class CorrelationId {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private CorrelationId() {}

    /**
     * Returns the incoming value when it is a canonical UUID, otherwise a fresh one.
     * Rejecting non-UUID values keeps arbitrary client input out of logs and messages.
     */
    public static String resolve(String candidate) {
        return isValid(candidate) ? candidate : generate();
    }

    public static boolean isValid(String candidate) {
        return candidate != null && UUID_PATTERN.matcher(candidate).matches();
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
