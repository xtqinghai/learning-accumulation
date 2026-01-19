package com.quinn.hboot.flow.adapter;


import com.quinn.hboot.flow.vo.WarmFlowInteractiveTypeVo;
import org.springframework.stereotype.Component;

@Component
public interface WarmFlowAdapter {
    boolean isAdapter(Integer warmFlowType);

    boolean adapter(WarmFlowInteractiveTypeVo obj);
}
