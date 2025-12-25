package com.quinn.hboot.system.converter;

import com.quinn.hboot.system.model.entity.SysFile;
import org.mapstruct.Mapper;
import com.quinn.hboot.system.model.form.FileForm;

/**
 * 文件管理对象转换器
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Mapper(componentModel = "spring")
public interface FileConverter{

    FileForm toForm(SysFile entity);

    SysFile toEntity(FileForm formData);
}
