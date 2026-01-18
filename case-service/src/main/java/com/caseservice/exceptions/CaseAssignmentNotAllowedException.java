package com.caseservice.exceptions;

public class CaseAssignmentNotAllowedException extends RuntimeException {
    public CaseAssignmentNotAllowedException(String message) {
        super(message);
    }
}