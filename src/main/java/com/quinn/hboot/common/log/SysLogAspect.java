package com.quinn.hboot.common.log;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.json.JSONUtil;
import com.quinn.hboot.common.trace.TraceContext;
import com.quinn.hboot.system.model.entity.OperLog;
import com.quinn.hboot.system.service.OperLogService;
import com.quinn.hboot.utils.IPUtils;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class SysLogAspect {

    private final OperLogService operLogService;

    @Around("@annotation(sysLog)")
    public Object around(ProceedingJoinPoint point, SysLog sysLog) throws Throwable {

        OperLog operLog = new OperLog();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();

        operLog.setModule(sysLog.module());
        operLog.setType(sysLog.type());
        operLog.setMethod(point.getSignature().toShortString());
        operLog.setOperTime(LocalDateTime.now());
        operLog.setOperParam(JSONUtil.toJsonStr(point.getArgs()));
        if (request != null) {
            operLog.setRequestMethod(request.getMethod());
            operLog.setOperUrl(request.getRequestURI());
            operLog.setOperIp(IPUtils.getIpAddr(request));
        }

        // 当前用户
        String username = SecurityUtils.getUsername();
        operLog.setOperName(username);

        TimeInterval timer = DateUtil.timer();
        String traceId = TraceContext.getTraceId();
        String businessId = TraceContext.getBusinessId();
        String instanceId = TraceContext.getInstanceId();

        try {
            Object result = point.proceed();
            operLog.setStatus(1);
            operLog.setJsonResult(JSONUtil.toJsonStr(result));
            log.info(
                "audit-oper success traceId={} businessId={} instanceId={} module={} type={} user={} method={}",
                traceId,
                businessId,
                instanceId,
                sysLog.module(),
                sysLog.type(),
                username,
                point.getSignature().toShortString()
            );
            return result;
        } catch (Throwable ex) {
            operLog.setStatus(0);
            operLog.setErrorMsg(ex.getMessage());
            log.warn(
                "audit-oper failed traceId={} businessId={} instanceId={} module={} type={} user={} method={} message={}",
                traceId,
                businessId,
                instanceId,
                sysLog.module(),
                sysLog.type(),
                username,
                point.getSignature().toShortString(),
                ex.getMessage()
            );
            throw ex;
        } finally {
            // 执行时长
            long executionTime = timer.interval();
            operLog.setExecutionTime(executionTime);
            // 异步保存
            operLogService.save(operLog);
        }
    }
}

