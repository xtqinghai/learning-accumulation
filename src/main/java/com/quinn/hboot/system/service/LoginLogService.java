package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.entity.LoginLog;
import com.quinn.hboot.system.model.form.LoginLogForm;
import com.quinn.hboot.system.model.query.LoginLogQuery;
import com.quinn.hboot.system.model.vo.LoginLogVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统登录日志服务类
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
public interface LoginLogService extends IService<LoginLog> {

    /**
     *系统登录日志分页列表
     *
     * @return {@link IPage<LoginLogVO>} 系统登录日志分页列表
     */
    IPage<LoginLogVO> getLoginLogPage(LoginLogQuery queryParams);

    /**
     * 获取系统登录日志表单数据
     *
     * @param id 系统登录日志ID
     * @return 系统登录日志表单数据
     */
     LoginLogForm getLoginLogFormData(Long id);

    /**
     * 新增系统登录日志
     *
     * @param formData 系统登录日志表单对象
     * @return 是否新增成功
     */
    boolean saveLoginLog(LoginLogForm formData);

    /**
     * 修改系统登录日志
     *
     * @param id   系统登录日志ID
     * @param formData 系统登录日志表单对象
     * @return 是否修改成功
     */
    boolean updateLoginLog(Long id, LoginLogForm formData);

    /**
     * 删除系统登录日志
     *
     * @param ids 系统登录日志ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    boolean deleteLoginLogs(String ids);

}
