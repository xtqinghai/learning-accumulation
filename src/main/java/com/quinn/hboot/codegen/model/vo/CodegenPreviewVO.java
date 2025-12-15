package com.quinn.hboot.codegen.model.vo;

import lombok.Data;

//@Schemadescription = "代码生成代码预览VO")
@Data
public class CodegenPreviewVO {

    //@Schemadescription = "生成文件路径")
    private String path;

    //@Schemadescription = "生成文件名称",example = "SysUser.java" )
    private String fileName;

    //@Schemadescription = "生成文件内容")
    private String content;

}
