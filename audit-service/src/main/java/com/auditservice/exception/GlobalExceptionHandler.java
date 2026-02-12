package com.auditservice.exception;

import com.govcaseflow.infrastructure.web.BaseGlobalExceptionHandler;
import com.govcaseflow.infrastructure.web.ErrorCode;
import com.govcaseflow.infrastructure.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
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
}
