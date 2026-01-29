package com.quinn.hboot.system.converter;

import com.quinn.hboot.system.model.entity.SysPost;
import org.mapstruct.Mapper;
import com.quinn.hboot.system.model.form.PostForm;

/**
 * 岗位信息对象转换器
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Mapper(componentModel = "spring")
public interface PostConverter{

    PostForm toForm(SysPost entity);

    SysPost toEntity(PostForm formData);
}
