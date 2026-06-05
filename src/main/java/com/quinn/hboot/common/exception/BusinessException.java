package com.quinn.hboot.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final ErrorCode errorCode;

    public BusinessException(String message) {
        this(ErrorCode.BUSINESS_RULE_FAILED, message);
    }

    public BusinessException(int code, String message) {
        this(ErrorCode.fromCode(code), message);
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message == null || message.isBlank() ? errorCode.getDefaultMessage() : message);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }
}
