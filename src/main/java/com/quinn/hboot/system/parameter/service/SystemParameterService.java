package com.quinn.hboot.system.parameter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.system.parameter.model.form.SystemParameterForm;
import com.quinn.hboot.system.parameter.model.query.SystemParameterQuery;
import com.quinn.hboot.system.parameter.model.vo.SystemParameterVO;

public interface SystemParameterService {

    IPage<SystemParameterVO> page(SystemParameterQuery query);

    SystemParameterVO get(Long id);

    Long create(SystemParameterForm form);

    void update(Long id, SystemParameterForm form);

    void delete(Long id);

    /** Intended for internal consumers only. Sensitive values are decrypted here and never exposed by controllers. */
    String getEnabledValue(String parameterKey);
}
