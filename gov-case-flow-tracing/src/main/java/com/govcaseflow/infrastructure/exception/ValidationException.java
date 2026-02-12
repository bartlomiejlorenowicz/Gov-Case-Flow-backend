package com.govcaseflow.infrastructure.exception;

import com.govcaseflow.infrastructure.web.FieldValidationError;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<FieldValidationError> fieldErrors;

    public ValidationException(String message, List<FieldValidationError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public List<FieldValidationError> getFieldErrors() {
        return fieldErrors;
    }
}

