package com.quinn.hboot.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应对象。
 *
 * @param <T> 响应数据类型
 */
@Data
public class R<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(HttpStatusCodeEnum.OK.getCode());
        r.setMessage("success");
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
        return r;
    }

    /**
     * 常用 HTTP 状态码枚举。
     */
    public enum HttpStatusCodeEnum {
        OK(200),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        TOO_MANY_REQUESTS(429),
        INTERNAL_SERVER_ERROR(500);

        private final int code;

        HttpStatusCodeEnum(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
