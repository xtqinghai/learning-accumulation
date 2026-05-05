package com.quinn.hboot.workflow.listener;

import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.listener.Listener;
import org.dromara.warm.flow.core.listener.ListenerVariable;
import org.springframework.stereotype.Component;

/**
 * 请假审核节点分配监听器，用于演示节点监听器挂载能力。
 */
@ConditionalOnEnable
@Component
@Slf4j
public class LeaveAuditAssignmentListener implements Listener {

    @Override
    public void notify(ListenerVariable listenerVariable) {
        log.info("LeaveAuditAssignmentListener triggered, instanceId={}, nodeCode={}, nextTaskSize={}",
            listenerVariable.getInstance() == null ? null : listenerVariable.getInstance().getId(),
            listenerVariable.getNode() == null ? null : listenerVariable.getNode().getNodeCode(),
            listenerVariable.getNextTasks() == null ? 0 : listenerVariable.getNextTasks().size());
    }
}
