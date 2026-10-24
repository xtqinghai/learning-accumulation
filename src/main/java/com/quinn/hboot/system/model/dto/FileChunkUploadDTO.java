package com.quinn.hboot.system.model.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @program: h-boot
 * @ClassName FileChunkUploadDTO
 * @description: 说明：TODO
 * @author: yuqh
 * @create: 2025-12-25 23:21
 * @Version 1.0
 **/
@Data
public class FileChunkUploadDTO {

    @NotBlank(message = "文件 hash 不能为空")
    private String fileHash;

    @NotNull(message = "分片序号不能为空")
    @Min(value = 0, message = "分片序号不能小于 0")
    private Integer chunkIndex;

    @NotNull(message = "分片总数不能为空")
    @Min(value = 1, message = "分片总数必须大于 0")
    private Integer totalChunks;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于 0")
    private Long fileSize;
}

