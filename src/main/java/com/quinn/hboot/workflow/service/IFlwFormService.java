package com.quinn.hboot.workflow.service;

import com.quinn.hboot.common.R;
import com.quinn.hboot.workflow.domain.PageQuery;
import com.quinn.hboot.workflow.domain.bo.FlwFormBo;
import com.quinn.hboot.workflow.domain.vo.FlwFormVo;

import java.util.Collection;

/**
 * 流程表单Service接口
 */
public interface IFlwFormService {

    FlwFormVo queryById(Long id);

    R queryPageList(FlwFormBo bo, PageQuery pageQuery);

    Boolean insertByBo(FlwFormBo bo);

    Boolean updateByBo(FlwFormBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);

    Boolean publish(Long id);

    Boolean unPublish(Long id);

    FlwFormVo copyForm(Long id);

    FlwFormVo getByCode(String formCode);
}
