package com.caseservice.exceptions;


public class CaseAccessDeniedException extends RuntimeException {
    public CaseAccessDeniedException(String message) {
        super(message);
    }
}