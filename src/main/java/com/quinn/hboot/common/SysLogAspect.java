//package com.quinn.hboot.common;
//
//import cn.hutool.json.JSONUtil;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.time.LocalDateTime;
//
///**
// * @program: h-boot
// * @ClassName SysLogAspect
// * @description: 说明：TODO
// * @author: yuqh
// * @create: 2025-12-21 00:38
// * @Version 1.0
// **/
//@Aspect
//@Component
//@RequiredArgsConstructor
//public class SysLogAspect {
//
//    private final SysOperLogService operLogService;
//
//    @Around("@annotation(sysLog)")
//    public Object around(ProceedingJoinPoint point, SysLog sysLog) throws Throwable {
//
//        SysOperLog log = new SysOperLog();
//        HttpServletRequest request =
//                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//
//        log.setModule(sysLog.module());
//        log.setType(sysLog.type());
//        log.setMethod(point.getSignature().toShortString());
//        log.setRequestMethod(request.getMethod());
//        log.setOperUrl(request.getRequestURI());
//        log.setOperIp(request.getRemoteAddr());
//        log.setOperTime(LocalDateTime.now());
//
//        // 当前用户
//        String username = SecurityContextHolder.getContext()
//                .getAuthentication().getName();
//        log.setOperName(username);
//
//        try {
//            Object result = point.proceed();
//            log.setStatus(1);
//            log.setJsonResult(JSONUtil.toJsonStr(result));
//            return result;
//        } catch (Exception e) {
//            log.setStatus(0);
//            log.setErrorMsg(e.getMessage());
//            throw e;
//        } finally {
//            // 异步保存
//            operLogService.saveAsync(log);
//        }
//    }
//}
//
