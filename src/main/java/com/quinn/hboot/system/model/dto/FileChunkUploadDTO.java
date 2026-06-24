package com.quinn.hboot.system.model.dto;

import lombok.Data;

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

    private String fileHash;
    private Integer chunkIndex;
    private Integer totalChunks;
    private String fileName;
    private Long fileSize;
}

