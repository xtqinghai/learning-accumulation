package com.quinn.hboot.system.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.OperLogMapper;
import com.quinn.hboot.system.service.OperLogService;
import com.quinn.hboot.system.model.entity.OperLog;
import com.quinn.hboot.system.model.form.OperLogForm;
import com.quinn.hboot.system.model.query.OperLogQuery;
import com.quinn.hboot.system.model.vo.OperLogVO;
import com.quinn.hboot.system.converter.OperLogConverter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

/**
 * 系统操作日志服务实现类
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements OperLogService {

    private final OperLogConverter operLogConverter;

    /**
    * 获取系统操作日志分页列表
    *
    * @param queryParams 查询参数
    * @return {@link IPage<OperLogVO>} 系统操作日志分页列表
    */
    @Override
    public IPage<OperLogVO> getOperLogPage(OperLogQuery queryParams) {
        Page<OperLogVO> pageVO = this.baseMapper.getOperLogPage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );
        return pageVO;
    }
    
    /**
     * 获取系统操作日志表单数据
     *
     * @param id 系统操作日志ID
     * @return 系统操作日志表单数据
     */
    @Override
    public OperLogForm getOperLogFormData(Long id) {
        OperLog entity = this.getById(id);
        return operLogConverter.toForm(entity);
    }
    
    /**
     * 新增系统操作日志
     *
     * @param formData 系统操作日志表单对象
     * @return 是否新增成功
     */
    @Override
    public boolean saveOperLog(OperLogForm formData) {
        OperLog entity = operLogConverter.toEntity(formData);
        return this.save(entity);
    }
    
    /**
     * 更新系统操作日志
     *
     * @param id   系统操作日志ID
     * @param formData 系统操作日志表单对象
     * @return 是否修改成功
     */
    @Override
    public boolean updateOperLog(Long id,OperLogForm formData) {
        OperLog entity = operLogConverter.toEntity(formData);
        return this.updateById(entity);
    }
    
    /**
     * 删除系统操作日志
     *
     * @param ids 系统操作日志ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    @Override
    public boolean deleteOperLogs(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的系统操作日志数据为空");
        // 逻辑删除
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        return this.removeByIds(idList);
    }

}
