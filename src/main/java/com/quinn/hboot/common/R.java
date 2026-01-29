package com.quinn.hboot.common;


import lombok.Data;

import java.io.Serializable;

@Data
public class R<T>  implements Serializable {

    private int code;
    private String message;
    private T data;

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("success");
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = ok();
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code,String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        System.err.println(message);
        return r;
    }

    /**
     * HTTP 状态码枚举类
     * 涵盖 RFC 标准中最常用的状态码，包含编码、官方描述、详细说明
     */
    public enum HttpStatusCodeEnum {

        // ====================== 1xx 信息类 - 服务器已接收请求，正在处理 ======================
        CONTINUE(100, "Continue", "客户端应继续发送请求的剩余部分，或如果请求已完成，忽略此响应"),
        SWITCHING_PROTOCOLS(101, "Switching Protocols", "服务器根据客户端的请求切换协议（如 HTTP 切换到 WebSocket）"),

        // ====================== 2xx 成功类 - 请求已成功处理 ======================
        OK(200, "OK", "请求成功，服务器正常返回数据（GET/POST/PUT 等通用成功状态）"),
        CREATED(201, "Created", "请求成功并创建了新资源（POST 新增数据成功）"),
        NO_CONTENT(204, "No Content", "请求成功，但服务器无数据返回（DELETE 删除成功）"),

        // ====================== 3xx 重定向类 - 需要客户端进一步操作 ======================
        MOVED_PERMANENTLY(301, "Moved Permanently", "永久重定向，资源已永久迁移到新地址"),
        FOUND(302, "Found", "临时重定向，资源临时迁移（如未登录跳转到登录页）"),
        NOT_MODIFIED(304, "Not Modified", "资源未修改，客户端可使用本地缓存（优化性能）"),

        // ====================== 4xx 客户端错误 - 请求有问题，服务器无法处理 ======================
        BAD_REQUEST(400, "Bad Request", "请求参数错误，服务器无法解析（参数类型不匹配/必填项缺失）"),
        UNAUTHORIZED(401, "Unauthorized", "未授权，需要身份验证（token 失效/未携带凭证）"),
        FORBIDDEN(403, "Forbidden", "拒绝访问，已认证但无权限操作（普通用户访问管理员接口）"),
        NOT_FOUND(404, "Not Found", "资源未找到（URL 错误/接口已删除）"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed", "请求方法不被允许（GET 访问仅支持 POST 的接口）"),
        REQUEST_TIMEOUT(408, "Request Timeout", "客户端请求超时，服务器等待过久未收到完整请求"),
        TOO_MANY_REQUESTS(429, "Too Many Requests", "请求过于频繁，触发限流（接口调用频率超限）"),

        // ====================== 5xx 服务端错误 - 服务器自身故障 ======================
        INTERNAL_SERVER_ERROR(500, "Internal Server Error", "服务器内部错误（代码异常/数据库连接失败等）"),
        BAD_GATEWAY(502, "Bad Gateway", "网关错误，上游服务器返回无效响应（Nginx 转发后端无响应）"),
        SERVICE_UNAVAILABLE(503, "Service Unavailable", "服务不可用，服务器过载或维护中"),
        GATEWAY_TIMEOUT(504, "Gateway Timeout", "网关超时，等待上游服务器响应超时");

        /**
         * HTTP 状态码（数字）
         */
        private final int code;

        /**
         * 官方简短描述（Reason-Phrase）
         */
        private final String reasonPhrase;

        /**
         * 详细说明（便于开发理解）
         */
        private final String description;

        /**
         * 构造方法
         */
        HttpStatusCodeEnum(int code, String reasonPhrase, String description) {
            this.code = code;
            this.reasonPhrase = reasonPhrase;
            this.description = description;
        }

        // ====================== 实用工具方法 ======================

        /**
         * 根据状态码数字获取对应的枚举项
         * @param code HTTP状态码（如 404）
         * @return 对应的枚举项，若不存在则返回 null
         */
        public static HttpStatusCodeEnum getByCode(int code) {
            for (HttpStatusCodeEnum status : values()) {
                if (status.getCode() == code) {
                    return status;
                }
            }
            return null;
        }

        /**
         * 判断状态码是否为成功类（2xx）
         */
        public boolean isSuccess() {
            return this.code >= 200 && this.code < 300;
        }

        /**
         * 判断状态码是否为客户端错误（4xx）
         */
        public boolean isClientError() {
            return this.code >= 400 && this.code < 500;
        }

        /**
         * 判断状态码是否为服务端错误（5xx）
         */
        public boolean isServerError() {
            return this.code >= 500 && this.code < 600;
        }

        // ====================== Getter 方法 ======================
        public int getCode() {
            return code;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public String getDescription() {
            return description;
        }
    }
}
