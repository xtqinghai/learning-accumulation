package com.quinn.hboot.codegen.model.vo;

import lombok.Data;


//@Schemaa(description = "表视图对象")
@Data
public class TablePageVO {

    //@Schemaa(description = "表名称", example = "sys_user")
    private String tableName;

    //@Schemaa(description = "表描述",example = "用户表")
    private String tableComment;

    //@Schemaa(description = "表排序规则",example = "utf8mb4_general_ci")
    private String tableCollation;

    //@Schemaa(description = "存储引擎",example = "InnoDB")
    private String engine;

    //@Schemaa(description = "字符集",example = "utf8mb4")
    private String charset;

    //@Schemaa(description = "创建时间",example = "2023-08-08 08:08:08")
    private String createTime;

    //@Schemaa(description="是否已配置")
    private Integer isConfigured;

}
