package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.entity.OperLog;
import com.quinn.hboot.system.model.form.OperLogForm;
import com.quinn.hboot.system.model.query.OperLogQuery;
import com.quinn.hboot.system.model.vo.OperLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统操作日志服务类
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
public interface OperLogService extends IService<OperLog> {

    /**
     *系统操作日志分页列表
     *
     * @return {@link IPage<OperLogVO>} 系统操作日志分页列表
     */
    IPage<OperLogVO> getOperLogPage(OperLogQuery queryParams);

    /**
     * 获取系统操作日志表单数据
     *
     * @param id 系统操作日志ID
     * @return 系统操作日志表单数据
     */
     OperLogForm getOperLogFormData(Long id);

    /**
     * 新增系统操作日志
     *
     * @param formData 系统操作日志表单对象
     * @return 是否新增成功
     */
    boolean saveOperLog(OperLogForm formData);

    /**
     * 修改系统操作日志
     *
     * @param id   系统操作日志ID
     * @param formData 系统操作日志表单对象
     * @return 是否修改成功
     */
    boolean updateOperLog(Long id, OperLogForm formData);

    /**
     * 删除系统操作日志
     *
     * @param ids 系统操作日志ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    boolean deleteOperLogs(String ids);

}
