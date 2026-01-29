package com.quinn.hboot.workflow.service.impl;

import cn.hutool.core.convert.Convert;
import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import com.quinn.hboot.workflow.common.constant.FlowConstant;
import com.quinn.hboot.workflow.domain.trans.TranslationType;
import com.quinn.hboot.workflow.service.IFlwCategoryService;
import com.quinn.hboot.workflow.service.TranslationInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.dromara.common.translation.annotation.TranslationType;
//import org.dromara.common.translation.core.TranslationInterface;
//import org.dromara.workflow.common.ConditionalOnEnable;
//import org.dromara.workflow.common.constant.FlowConstant;
//import org.dromara.workflow.service.IFlwCategoryService;
import org.springframework.stereotype.Service;

/**
 * 流程分类名称翻译实现
 *
 * @author AprilWind
 */
@ConditionalOnEnable
@Slf4j
@RequiredArgsConstructor
@Service
@TranslationType(type = FlowConstant.CATEGORY_ID_TO_NAME)
public class CategoryNameTranslationImpl implements TranslationInterface<String> {

    private final IFlwCategoryService flwCategoryService;

    @Override
    public String translation(Object key, String other) {
        return flwCategoryService.selectCategoryNameById(Convert.toLong(key));
    }
}
