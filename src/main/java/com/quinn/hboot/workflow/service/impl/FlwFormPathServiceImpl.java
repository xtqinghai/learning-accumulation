package com.quinn.hboot.workflow.service.impl;

import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import com.quinn.hboot.workflow.domain.FlwForm;
import com.quinn.hboot.workflow.mapper.FlwFormMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.dto.Tree;
import org.dromara.warm.flow.ui.service.FormPathService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 给 Warm-Flow 内置设计器提供外挂动态表单路径
 */
@ConditionalOnEnable
@RequiredArgsConstructor
@Service
public class FlwFormPathServiceImpl implements FormPathService {

    private final FlwFormMapper baseMapper;

    @Override
    public List<Tree> queryFormPath() {
        List<FlwForm> forms = baseMapper.selectList(Wrappers.lambdaQuery(FlwForm.class)
            .eq(FlwForm::getIsPublish, 1)
            .orderByAsc(FlwForm::getFormCode));
        List<Tree> trees = new ArrayList<>();
        for (FlwForm form : forms) {
            trees.add(new Tree()
                .setId("form:" + form.getFormCode())
                .setName(form.getFormName() + " [" + form.getFormCode() + "]")
                .setParentId("0"));
        }
        return trees;
    }
}
