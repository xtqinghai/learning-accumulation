package com.quinn.hboot.workflow.mapper;

import com.quinn.hboot.workflow.common.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;
import com.quinn.hboot.workflow.domain.FlwForm;
import com.quinn.hboot.workflow.domain.vo.FlwFormVo;

/**
 * 流程表单Mapper接口
 */
public interface FlwFormMapper extends BaseMapperPlus<FlwForm, FlwFormVo> {

    long countPublishedDefinitionUsage(@Param("formPath") String formPath);
}
