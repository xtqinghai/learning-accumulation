package com.quinn.hboot.codegen.converter;

import com.quinn.hboot.codegen.model.entity.GenConfig;
import com.quinn.hboot.codegen.model.entity.GenFieldConfig;
import com.quinn.hboot.codegen.model.form.GenConfigForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 代码生成配置转换器
 *
 * @author Ray
 * @since 2.10.0
 */
@Mapper(componentModel = "spring")
public interface CodegenConverter {

    // 👇 把 genConfig 整体映射到 GenConfigForm
    @Mapping(target = ".", source = "genConfig")
    // 👇 单独映射字段配置列表
    @Mapping(target = "fieldConfigs", source = "fieldConfigs")
    GenConfigForm toGenConfigForm(GenConfig genConfig, List<GenFieldConfig> fieldConfigs);

    List<GenConfigForm.FieldConfig> toGenFieldConfigForm(List<GenFieldConfig> fieldConfigs);

    GenConfigForm.FieldConfig toGenFieldConfigForm(GenFieldConfig genFieldConfig);

    GenConfig toGenConfig(GenConfigForm formData);

    List<GenFieldConfig> toGenFieldConfig(List<GenConfigForm.FieldConfig> fieldConfigs);

    GenFieldConfig toGenFieldConfig(GenConfigForm.FieldConfig fieldConfig);

}
