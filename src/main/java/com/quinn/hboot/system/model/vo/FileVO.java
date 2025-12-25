package com.quinn.hboot.system.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * 文件管理视图对象
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Getter
@Setter
@Schema( description = "文件管理视图对象")
public class FileVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "原始文件名")
    private String fileName;
    @Schema(description = "扩展名")
    private String fileExt;
    @Schema(description = "MIME类型")
    private String contentType;
    @Schema(description = "文件大小(byte)")
    private Long fileSize;
    @Schema(description = "文件hash(SHA256/MD5)")
    private String fileHash;
    @Schema(description = "存储类型")
    private String storageType;
    @Schema(description = "存储路径或对象key")
    private String storagePath;
    @Schema(description = "访问URL")
    private String url;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
