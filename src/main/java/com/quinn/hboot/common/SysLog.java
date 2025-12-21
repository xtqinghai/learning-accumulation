package com.quinn.hboot.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @program: h-boot
 * @ClassName SysLog
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2025-12-21 00:18
 * @Version 1.0
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SysLog {

    /** 模块名称 */
    String module() default "";

    /** 操作类型 */
    String type() default "";
}

