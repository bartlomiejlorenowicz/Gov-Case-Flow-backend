package com.govcaseflow.infrastructure.web;

public record FieldValidationError(
        String field,
        String message
) {}
