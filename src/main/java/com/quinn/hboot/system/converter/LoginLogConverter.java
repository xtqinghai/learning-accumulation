package com.quinn.hboot.system.converter;

import org.mapstruct.Mapper;
import com.quinn.hboot.system.model.entity.LoginLog;
import com.quinn.hboot.system.model.form.LoginLogForm;

/**
 * 系统登录日志对象转换器
 *
 * @author yu-qh
 * @since 2025-12-21 18:35
 */
@Mapper(componentModel = "spring")
public interface LoginLogConverter{

    LoginLogForm toForm(LoginLog entity);

    LoginLog toEntity(LoginLogForm formData);
}
