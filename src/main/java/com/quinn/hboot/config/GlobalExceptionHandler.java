package com.quinn.hboot.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<String> handleBusinessException(BusinessException ex) {
        return R.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<String> handleNotLoginException(NotLoginException ex) {
        return R.fail(401, "未登录或登录状态已失效");
    }

    @ExceptionHandler(NotPermissionException.class)
    public R<String> handleNotPermissionException(NotPermissionException ex) {
        return R.fail(403, "无权限访问当前资源");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public R<String> handleValidationException(Exception ex) {
        String message;
        if (ex instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            message = methodArgumentNotValidException.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("；"));
        } else {
            BindException bindException = (BindException) ex;
            message = bindException.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("；"));
        }
        return R.fail(400, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return R.fail(400, "请求体格式不正确，请检查提交内容");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return R.fail(400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception ex) {
        log.error("系统处理异常", ex);
        return R.fail(500, "系统内部异常，请联系管理员");
    }
}
