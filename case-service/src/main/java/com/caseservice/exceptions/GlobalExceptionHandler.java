package com.caseservice.exceptions;

import com.govcaseflow.infrastructure.web.BaseGlobalExceptionHandler;
import com.govcaseflow.infrastructure.web.ErrorCode;
import com.govcaseflow.infrastructure.web.ErrorResponse;
import com.govcaseflow.infrastructure.web.FieldValidationError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(InvalidCaseStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCaseStatusTransitionException(HttpServletRequest request,
                                                                                    InvalidCaseStatusTransitionException ex) {
        return buildResponse(
                request,
                HttpStatus.CONFLICT,
                ErrorCode.BUSINESS_RULE_VIOLATION,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CaseAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCaseAlreadyExistsException(HttpServletRequest request, CaseAlreadyExistsException ex) {

        log.error("HANDLER HIT for CaseAlreadyExistsException");

        return buildResponse(
                request,
                HttpStatus.CONFLICT,
                ErrorCode.BUSINESS_RULE_VIOLATION,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCaseNotFoundException(HttpServletRequest request,
                                                                     CaseNotFoundException ex) {
        return buildResponse(
                request,
                HttpStatus.NOT_FOUND,
                ErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CaseAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleCaseAlreadyAssignedException(HttpServletRequest request,
                                                                            CaseAlreadyAssignedException ex) {
        return buildResponse(
                request,
                HttpStatus.CONFLICT,
                ErrorCode.BUSINESS_RULE_VIOLATION,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CaseAssignmentNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleCaseAssignmentNotAllowedException(HttpServletRequest request,
                                                                                 CaseAssignmentNotAllowedException ex) {
        return buildResponse(
                request,
                HttpStatus.CONFLICT,
                ErrorCode.BUSINESS_RULE_VIOLATION,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(CaseAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleCaseAccessDeniedException(HttpServletRequest request, CaseAccessDeniedException ex) {
        return buildResponse(
                request,
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                ex.getMessage(),
              null
        );
    }

//    @Override
//    protected ResponseEntity<Object> handleMethodArgumentNotValid(
//            MethodArgumentNotValidException ex,
//            HttpHeaders headers,
//            HttpStatusCode status,
//            WebRequest request
//    ) {
//
//        HttpServletRequest servletRequest =
//                ((ServletWebRequest) request).getRequest();
//
//        List<FieldValidationError> fieldValidationErrors =
//                ex.getBindingResult()
//                        .getFieldErrors()
//                        .stream()
//                        .map(error -> new FieldValidationError(
//                                error.getField(),
//                                error.getDefaultMessage()
//                        ))
//                        .toList();
//
//        ErrorResponse errorResponse = new ErrorResponse(
//                Instant.now(),
//                status.value(),
//                ErrorCode.VALIDATION_ERROR,
//                "Validation failed",
//                servletRequest.getRequestURI(),
//                MDC.get("traceId"),
//                fieldValidationErrors
//        );
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }
}
