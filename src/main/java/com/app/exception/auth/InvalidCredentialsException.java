package com.app.exception.auth;

import com.app.exception.BusinessException;
import com.app.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
    }
}
