package com.app.exception.auth;

import com.app.exception.BusinessException;
import com.app.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class DisabledAccountException extends BusinessException {

    public DisabledAccountException() {
        super(ErrorCode.DISABLED_ACCOUNT, HttpStatus.UNAUTHORIZED);
    }
}
