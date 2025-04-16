package com.app.exception;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildError("Validación fallida: " + message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        return buildError("Error inesperado", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ApiErrorResponse> buildError(String message, HttpStatus status, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .message(message)
                .status(status.value())
                .error(status.getReasonPhrase())
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(RequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(RequestException ex, HttpServletRequest request) {
        ApiErrorResponse error = ApiErrorResponse.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .timestamp(Instant.now().toString())
                .build();

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

}
