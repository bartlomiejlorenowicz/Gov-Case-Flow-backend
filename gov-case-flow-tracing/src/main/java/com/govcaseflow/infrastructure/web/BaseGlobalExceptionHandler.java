package com.govcaseflow.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;

public abstract class BaseGlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {

        HttpServletRequest servletRequest =
                ((ServletWebRequest) request).getRequest();

        List<FieldValidationError> fieldValidationErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> new FieldValidationError(
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .toList();

        ErrorResponse errorBody = buildErrorBody(
                servletRequest,
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Validation failed",
                fieldValidationErrors
        );

        return ResponseEntity.badRequest().body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            HttpServletRequest request,
            Exception ex
    ) {
        return buildResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "Unexpected error occurred",
                null
        );
    }

    protected ResponseEntity<ErrorResponse> buildResponse(
            HttpServletRequest request,
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            List<FieldValidationError> fieldErrors
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                MDC.get("traceId"),
                fieldErrors
        );

        return ResponseEntity.status(status).body(errorResponse);
    }

    protected ErrorResponse buildErrorBody(
            HttpServletRequest request,
            HttpStatus status,
            ErrorCode errorCode,
            String message,
            List<FieldValidationError> fieldErrors
    ) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                MDC.get("traceId"),
                fieldErrors
        );
    }
}
