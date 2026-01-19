package com.quinn.hboot.flow.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.quinn.hboot.flow.vo.FlowTaskVo;
import com.quinn.hboot.flow.vo.WarmFlowInteractiveTypeVo;

/**
 * 流程定义service
 *
 * @author warm
 * @since 2023/5/29 13:09
 */
public interface HhDefService  extends IService<FlowTaskVo> {

    Boolean interactiveType(WarmFlowInteractiveTypeVo warmFlowInteractiveTypeVo);
}
