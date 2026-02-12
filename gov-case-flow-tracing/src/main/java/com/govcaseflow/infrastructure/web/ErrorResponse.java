package com.govcaseflow.infrastructure.web;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        ErrorCode error,
        String message,
        String path,
        String traceId,
        List<FieldValidationError> fieldErrors
) {}
