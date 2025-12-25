package com.quinn.hboot.system.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;

/**
 * 文件管理实体对象
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Getter
@Setter
@TableName("sys_file")
public class SysFile extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原始文件名
     */
    private String fileName;
    /**
     * 扩展名
     */
    private String fileExt;
    /**
     * MIME类型
     */
    private String contentType;
    /**
     * 文件大小(byte)
     */
    private Long fileSize;
    /**
     * 文件hash(SHA256/MD5)
     */
    private String fileHash;
    /**
     * 存储类型
     */
    private String storageType;
    /**
     * 存储路径或对象key
     */
    private String storagePath;
    /**
     * 访问URL
     */
    private String url;
    /**
     * 状态
     */
    private Integer status;
    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
