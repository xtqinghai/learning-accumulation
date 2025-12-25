package com.quinn.hboot.system.model.form;

import java.io.Serial;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

/**
 * 文件管理表单对象
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Getter
@Setter
@Schema(description = "文件管理表单对象")
public class FileForm implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "不能为空")
    private Long id;

    @Schema(description = "原始文件名")
    @NotBlank(message = "原始文件名不能为空")
    @Size(max=255, message="原始文件名长度不能超过255个字符")
    private String fileName;

    @Schema(description = "扩展名")
    @Size(max=50, message="扩展名长度不能超过50个字符")
    private String fileExt;

    @Schema(description = "MIME类型")
    @Size(max=100, message="MIME类型长度不能超过100个字符")
    private String contentType;

    @Schema(description = "文件大小(byte)")
    private Long fileSize;

    @Schema(description = "文件hash(SHA256/MD5)")
    @NotBlank(message = "文件hash(SHA256/MD5)不能为空")
    @Size(max=64, message="文件hash(SHA256/MD5)长度不能超过64个字符")
    private String fileHash;

    @Schema(description = "存储类型")
    @Size(max=20, message="存储类型长度不能超过20个字符")
    private String storageType;

    @Schema(description = "存储路径或对象key")
    @Size(max=500, message="存储路径或对象key长度不能超过500个字符")
    private String storagePath;

    @Schema(description = "访问URL")
    @Size(max=500, message="访问URL长度不能超过500个字符")
    private String url;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


}
