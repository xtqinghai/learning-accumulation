package com.quinn.hboot.common.log;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.quinn.hboot.system.model.entity.OperLog;
import com.quinn.hboot.system.service.OperLogService;
import com.quinn.hboot.utils.IPUtils;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * @program: h-boot
 * @ClassName SysLogAspect
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2025-12-21 00:38
 * @Version 1.0
 **/
@Aspect
@Component
@RequiredArgsConstructor
public class SysLogAspect {

    private final OperLogService operLogService;

    @Around("@annotation(sysLog)")
    public Object around(ProceedingJoinPoint point, SysLog sysLog) throws Throwable {

        OperLog log = new OperLog();
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        log.setModule(sysLog.module());
        log.setType(sysLog.type());
        log.setMethod(point.getSignature().toShortString());
        log.setRequestMethod(request.getMethod());
        log.setOperUrl(request.getRequestURI());
        log.setOperIp(IPUtils.getIpAddr(request));
        log.setOperTime(LocalDateTime.now());
        log.setOperParam(JSONUtil.toJsonStr(point.getArgs()));

        // 当前用户
        String username = SecurityUtils.getUsername();
        log.setOperName(username);

        TimeInterval timer = DateUtil.timer();

        try {
            Object result = point.proceed();
            log.setStatus(1);
            log.setJsonResult(JSONUtil.toJsonStr(result));
            return result;
        } catch (Exception e) {
            log.setStatus(0);
            log.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            // 执行时长
            long executionTime = timer.interval();
            log.setExecutionTime(executionTime);
            // 异步保存
            operLogService.save(log);
        }
    }
}

