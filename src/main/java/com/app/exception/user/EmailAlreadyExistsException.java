package com.app.exception.user;

import com.app.exception.BusinessException;
import com.app.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException() {
        super(ErrorCode.EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
    }
}
