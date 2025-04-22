package com.app.exception.user;

import com.app.exception.BusinessException;
import com.app.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
