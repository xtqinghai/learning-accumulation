package com.quinn.hboot.system.converter;

import org.mapstruct.Mapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.system.model.entity.CmsContent;
import com.quinn.hboot.system.model.form.CmsContentForm;

/**
 * 系统内容管理对象转换器
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Mapper(componentModel = "spring")
public interface CmsContentConverter{

    CmsContentForm toForm(CmsContent entity);

    CmsContent toEntity(CmsContentForm formData);
}