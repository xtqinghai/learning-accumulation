package com.quinn.hboot.common.exception;

import org.springframework.http.HttpStatus;

import java.util.Arrays;

public enum ErrorCode {

    OK(200, HttpStatus.OK, "success"),
    BAD_REQUEST(400, HttpStatus.BAD_REQUEST, "Bad request"),
    VALIDATION_ERROR(400, HttpStatus.BAD_REQUEST, "Validation failed"),
    BUSINESS_RULE_FAILED(400, HttpStatus.BAD_REQUEST, "Business rule failed"),
    UNAUTHORIZED(401, HttpStatus.UNAUTHORIZED, "Unauthorized"),
    FORBIDDEN(403, HttpStatus.FORBIDDEN, "Forbidden"),
    NOT_FOUND(404, HttpStatus.NOT_FOUND, "Resource not found"),
    TOO_MANY_REQUESTS(429, HttpStatus.TOO_MANY_REQUESTS, "Too many requests"),
    WORKFLOW_ERROR(400, HttpStatus.BAD_REQUEST, "Workflow error"),
    SYSTEM_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final int code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(int code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public static ErrorCode fromCode(int code) {
        return Arrays.stream(values())
            .filter(item -> item.code == code)
            .findFirst()
            .orElse(SYSTEM_ERROR);
    }
}
