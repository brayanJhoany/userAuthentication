package com.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;

    protected BusinessException(ErrorCode errorCode, HttpStatus status) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.httpStatus  = status;
    }
}
