package com.quinn.hboot.config;

import com.quinn.hboot.common.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yu-qh
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public R<String> handleException(Exception e) {
        return R.fail(500,e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public R<String> handleRuntimeException(RuntimeException e) {
        return R.fail(500,e.getMessage());
    }
}
