package com.app.exception;


import com.app.exception.user.WeakPasswordException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ← Excepciones propias de negocio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            BusinessException ex, HttpServletRequest request) {

        return buildError(ex.getErrorCode(),
                ex.getHttpStatus(), request);
    }

    // ← Validaciones de @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildError(ErrorCode.VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST, req, detail);
    }

    // ← Cualquier cosa no controlada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {

        return buildError(ErrorCode.UNEXPECTED_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    /* ---------- helpers ---------- */

    private ResponseEntity<ApiErrorResponse> buildError(
            ErrorCode code, HttpStatus status,
            HttpServletRequest req) {

        return buildError(code, status, req, code.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildError(
            ErrorCode code, HttpStatus status,
            HttpServletRequest req, String message) {

        ApiErrorResponse body = ApiErrorResponse.builder()
                .code(code.name())
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest req) {

        return buildError(ErrorCode.INVALID_CREDENTIALS,
                HttpStatus.UNAUTHORIZED, req);
    }
    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleWeakPassword(
            WeakPasswordException ex, HttpServletRequest req) {

        return buildError(ErrorCode.WEAK_PASSWORD,
                HttpStatus.BAD_REQUEST, req, ex.getMessage());
    }
}
