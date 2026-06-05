package com.quinn.hboot.common;

import com.quinn.hboot.common.exception.ErrorCode;
import com.quinn.hboot.common.trace.TraceContext;
import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private String traceId;

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(ErrorCode.OK.getCode());
        r.setMessage(ErrorCode.OK.getDefaultMessage());
        r.setTraceId(TraceContext.getTraceId());
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = ok();
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        r.setTraceId(TraceContext.getTraceId());
        return r;
    }

    public static <T> R<T> fail(ErrorCode errorCode, String message) {
        String finalMessage = message == null || message.isBlank() ? errorCode.getDefaultMessage() : message;
        return fail(errorCode.getCode(), finalMessage);
    }
}
