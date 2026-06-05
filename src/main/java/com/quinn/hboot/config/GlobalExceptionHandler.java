package com.quinn.hboot.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.common.exception.ErrorCode;
import com.quinn.hboot.common.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<String>> handleBusinessException(BusinessException ex) {
        log.warn(
            "Business exception, traceId={}, businessId={}, instanceId={}, code={}, message={}",
            TraceContext.getTraceId(),
            TraceContext.getBusinessId(),
            TraceContext.getInstanceId(),
            ex.getCode(),
            ex.getMessage()
        );
        return buildResponse(ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<String>> handleNotLoginException(NotLoginException ex) {
        return buildResponse(ErrorCode.UNAUTHORIZED, "未登录或登录状态已失效");
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<R<String>> handleNotPermissionException(NotPermissionException ex) {
        return buildResponse(ErrorCode.FORBIDDEN, "无权限访问当前资源");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<R<String>> handleValidationException(Exception ex) {
        String message;
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            message = methodArgumentNotValidException.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("，"));
        } else {
            BindException bindException = (BindException) ex;
            message = bindException.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("，"));
        }
        return buildResponse(ErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildResponse(ErrorCode.BAD_REQUEST, "请求体格式不正确，请检查提交内容");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponse(ErrorCode.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<String>> handleException(Exception ex) {
        log.error(
            "Unhandled exception, traceId={}, businessId={}, instanceId={}",
            TraceContext.getTraceId(),
            TraceContext.getBusinessId(),
            TraceContext.getInstanceId(),
            ex
        );
        return buildResponse(ErrorCode.SYSTEM_ERROR, "系统内部异常，请联系管理员");
    }

    private ResponseEntity<R<String>> buildResponse(ErrorCode errorCode, String message) {
        HttpStatus status = errorCode.getHttpStatus();
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(R.fail(errorCode, message));
    }
}
