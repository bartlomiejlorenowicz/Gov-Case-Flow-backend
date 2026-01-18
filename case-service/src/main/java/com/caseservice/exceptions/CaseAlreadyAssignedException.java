package com.caseservice.exceptions;

public class CaseAlreadyAssignedException extends RuntimeException {
    public CaseAlreadyAssignedException(String message) {
        super(message);
    }
}
