package com.authservice.exception;

import com.govcaseflow.infrastructure.web.BaseGlobalExceptionHandler;
import com.govcaseflow.infrastructure.web.ErrorCode;
import com.govcaseflow.infrastructure.web.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException extends BaseGlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(HttpServletRequest request,UserAlreadyExistsException ex) {
        return buildResponse(
            request,
                HttpStatus.CONFLICT,
                ErrorCode.USER_ALREADY_EXISTS,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(HttpServletRequest request, InvalidCredentialsException ex) {
        return buildResponse(
                request,
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_CREDENTIALS,
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(HttpServletRequest request, InvalidTokenException ex) {
        return buildResponse(
                request,
                HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_TOKEN,
                ex.getMessage(),
                null
        );
    }
}
