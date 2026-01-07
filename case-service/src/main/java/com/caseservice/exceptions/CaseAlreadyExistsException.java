package com.caseservice.exceptions;

public class CaseAlreadyExistsException extends RuntimeException {
    public CaseAlreadyExistsException(String message) {
        super(message);
    }
}
