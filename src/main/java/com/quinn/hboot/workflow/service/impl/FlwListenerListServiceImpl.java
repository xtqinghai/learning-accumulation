package com.quinn.hboot.workflow.service.impl;

import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import com.quinn.hboot.workflow.listener.LeaveAuditAssignmentListener;
import com.quinn.hboot.workflow.listener.LeaveFinishListener;
import com.quinn.hboot.workflow.listener.LeaveSubmitCreateListener;
import com.quinn.hboot.workflow.listener.WorkflowGlobalListener;
import org.dromara.warm.flow.core.listener.Listener;
import org.dromara.warm.flow.ui.service.ListenerListService;
import org.dromara.warm.flow.ui.vo.ListenerVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Warm-Flow 设计器监听器下拉数据源。
 */
@ConditionalOnEnable
@Service
public class FlwListenerListServiceImpl implements ListenerListService {

    @Override
    public List<ListenerVo> listenerList() {
        return List.of(
            new ListenerVo(
                Listener.LISTENER_CREATE,
                LeaveSubmitCreateListener.class.getName(),
                "请假申请节点创建监听器"
            ),
            new ListenerVo(
                Listener.LISTENER_ASSIGNMENT,
                LeaveAuditAssignmentListener.class.getName(),
                "请假审核节点分配监听器"
            ),
            new ListenerVo(
                Listener.LISTENER_FINISH,
                LeaveFinishListener.class.getName(),
                "请假流程结束监听器"
            ),
            new ListenerVo(
                Listener.LISTENER_FINISH,
                WorkflowGlobalListener.class.getName(),
                "全局流程完成监听器示例"
            )
        );
    }
}
