package com.quinn.hboot.flow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.flow.adapter.WarmFlowAdapter;
import com.quinn.hboot.flow.mapper.WarmFlowMapper;
import com.quinn.hboot.flow.service.HhDefService;
import com.quinn.hboot.flow.vo.FlowTaskVo;
import com.quinn.hboot.flow.vo.WarmFlowInteractiveTypeVo;
import com.quinn.hboot.utils.SpringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.core.service.NodeService;
import org.dromara.warm.flow.core.service.SkipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 流程定义serviceImpl
 *
 * @author warm
 * @since 2023/5/29 13:09
 */
@Service
public class HhDefServiceImpl extends ServiceImpl<WarmFlowMapper, FlowTaskVo>  implements HhDefService {

//    @Resource
//    private DefService defService;

//    @Resource
//    private NodeService nodeService;

//    @Resource
//    private SkipService skipService;

    private static final List<WarmFlowAdapter> WARM_FLOW_ADAPTERS = new ArrayList<>();

    /**
     * 初始化方法
     */
    @PostConstruct
    public void initMethod() {
        Map<String, WarmFlowAdapter> beanNamesForType = SpringUtils.getBeanNamesForType(WarmFlowAdapter.class);
        WARM_FLOW_ADAPTERS.addAll(beanNamesForType.values());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean interactiveType(WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo) {
        Integer operatorType = warmFlowInteractiveTypeVo.getOperatorType();
        for (WarmFlowAdapter warmFlowAdapter : WARM_FLOW_ADAPTERS) {
            if (warmFlowAdapter.isAdapter(operatorType)) {
                return warmFlowAdapter.adapter(warmFlowInteractiveTypeVo);
            }
        }
        return false;
    }



}
