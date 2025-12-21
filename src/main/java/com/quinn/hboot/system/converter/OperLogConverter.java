package com.quinn.hboot.system.converter;

import org.mapstruct.Mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.entity.OperLog;
import com.quinn.hboot.system.model.form.OperLogForm;

/**
 * 系统操作日志对象转换器
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Mapper(componentModel = "spring")
public interface OperLogConverter{

    OperLogForm toForm(OperLog entity);

    OperLog toEntity(OperLogForm formData);
}