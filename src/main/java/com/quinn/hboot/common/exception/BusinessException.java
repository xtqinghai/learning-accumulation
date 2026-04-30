package com.quinn.hboot.common.exception;

import lombok.Getter;

/**
 * 业务异常。
 * <p>
 * 用于承载可预期的业务校验失败场景，
 * 例如登录失败、参数非法、状态不允许等。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
