package com.caseservice.exceptions;

public class InvalidCaseStatusTransitionException extends RuntimeException{

    public InvalidCaseStatusTransitionException(String message) {
        super(message);
    }
}
