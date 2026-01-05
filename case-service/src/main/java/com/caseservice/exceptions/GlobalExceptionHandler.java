package com.caseservice.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCaseStatusTransitionException.class)
    public ResponseEntity<String> handleInvalidCaseStatusTransitionException(InvalidCaseStatusTransitionException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
