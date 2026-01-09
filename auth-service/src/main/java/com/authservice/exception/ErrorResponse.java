package com.authservice.exception;

public record ErrorResponse(
        int status,
        String message
) {}