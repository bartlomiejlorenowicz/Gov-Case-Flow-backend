package com.caseservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCaseStatusTransitionException.class)
    public ResponseEntity<String> handleInvalidCaseStatusTransitionException(InvalidCaseStatusTransitionException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(CaseAlreadyExistsException.class)
    public ResponseEntity<String> handleCaseAlreadyExistsException(CaseAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<String> handleCaseNotFoundException(CaseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CaseAlreadyAssignedException.class)
    public ResponseEntity<String> handleCaseAlreadyAssignedException(CaseAlreadyAssignedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(CaseAssignmentNotAllowedException.class)
    public ResponseEntity<String> handleCaseAssignmentNotAllowedException(CaseAssignmentNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CaseAccessDeniedException.class)
    public ResponseEntity<String> handleCaseAccessDeniedException(CaseAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}
