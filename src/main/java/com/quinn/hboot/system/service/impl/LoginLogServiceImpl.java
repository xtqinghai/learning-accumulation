package com.quinn.hboot.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.LoginLogMapper;
import com.quinn.hboot.system.service.LoginLogService;
import com.quinn.hboot.system.model.entity.LoginLog;
import com.quinn.hboot.system.model.form.LoginLogForm;
import com.quinn.hboot.system.model.query.LoginLogQuery;
import com.quinn.hboot.system.model.vo.LoginLogVO;
import com.quinn.hboot.system.converter.LoginLogConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

/**
 * 系统登录日志服务实现类
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    private final LoginLogConverter loginLogConverter;

    /**
    * 获取系统登录日志分页列表
    *
    * @param queryParams 查询参数
    * @return {@link IPage<LoginLogVO>} 系统登录日志分页列表
    */
    @Override
    public IPage<LoginLogVO> getLoginLogPage(LoginLogQuery queryParams) {
        Page<LoginLogVO> pageVO = this.baseMapper.getLoginLogPage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );
        return pageVO;
    }
    
    /**
     * 获取系统登录日志表单数据
     *
     * @param id 系统登录日志ID
     * @return 系统登录日志表单数据
     */
    @Override
    public LoginLogForm getLoginLogFormData(Long id) {
        LoginLog entity = this.getById(id);
        return loginLogConverter.toForm(entity);
    }
    
    /**
     * 新增系统登录日志
     *
     * @param formData 系统登录日志表单对象
     * @return 是否新增成功
     */
    @Override
    public boolean saveLoginLog(LoginLogForm formData) {
        LoginLog entity = loginLogConverter.toEntity(formData);
        return this.save(entity);
    }
    
    /**
     * 更新系统登录日志
     *
     * @param id   系统登录日志ID
     * @param formData 系统登录日志表单对象
     * @return 是否修改成功
     */
    @Override
    public boolean updateLoginLog(Long id,LoginLogForm formData) {
        LoginLog entity = loginLogConverter.toEntity(formData);
        return this.updateById(entity);
    }
    
    /**
     * 删除系统登录日志
     *
     * @param ids 系统登录日志ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    @Override
    public boolean deleteLoginLogs(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的系统登录日志数据为空");
        // 逻辑删除
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        return this.removeByIds(idList);
    }

}
