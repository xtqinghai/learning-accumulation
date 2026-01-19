package com.quinn.hboot.module.flow.listener;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.quinn.hboot.module.flow.mapper.TestLeaveMapper;
import jakarta.annotation.Resource;
import org.dromara.warm.flow.core.entity.Instance;
import org.dromara.warm.flow.core.listener.Listener;
import org.dromara.warm.flow.core.listener.ListenerVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FinishListener implements Listener {

    @Resource
    private TestLeaveMapper testLeaveMapper;

    private static final Logger log = LoggerFactory.getLogger(FinishListener.class);

    @Override
    public void notify(ListenerVariable listenerVariable) {
        log.info("完成监听器......");
        Instance instance = listenerVariable.getInstance();
        Map<String, Object> variable = listenerVariable.getVariable();
        if (ObjectUtils.isNotNull(variable)) {
            String businessId = instance.getBusinessId();
            Object businessType = variable.get("businessType");
        }
        log.info("完成监听器结束......");
    }
}
