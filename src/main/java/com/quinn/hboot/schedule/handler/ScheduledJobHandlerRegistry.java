package com.quinn.hboot.schedule.handler;

import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.schedule.model.vo.ScheduledJobHandlerVO;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScheduledJobHandlerRegistry {

    private final Map<String, ScheduledJobHandler> handlers;

    public ScheduledJobHandlerRegistry(Collection<ScheduledJobHandler> handlers) {
        this.handlers = new LinkedHashMap<>();
        for (ScheduledJobHandler handler : handlers) {
            if (this.handlers.putIfAbsent(handler.key(), handler) != null) {
                throw new IllegalStateException("定时任务处理器键重复: " + handler.key());
            }
        }
    }

    public ScheduledJobHandler getRequired(String key) {
        ScheduledJobHandler handler = handlers.get(key);
        if (handler == null) {
            throw new BusinessException("未注册的任务处理器: " + key);
        }
        return handler;
    }

    public List<ScheduledJobHandlerVO> list() {
        return handlers.values().stream()
            .map(handler -> new ScheduledJobHandlerVO(handler.key(), handler.name()))
            .toList();
    }
}
