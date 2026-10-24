package com.quinn.hboot.system.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.converter.FileConverter;
import com.quinn.hboot.system.mapper.FileMapper;
import com.quinn.hboot.system.model.dto.FileChunkUploadDTO;
import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.model.form.FileForm;
import com.quinn.hboot.system.model.query.FileQuery;
import com.quinn.hboot.system.model.vo.FileVO;
import com.quinn.hboot.system.service.FileService;
import com.quinn.hboot.system.service.FileUploadSecurityService;
import com.quinn.hboot.system.storage.FileStorageAdapter;
import com.quinn.hboot.utils.SecurityUtils;
import com.quinn.hboot.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage", havingValue = "local", matchIfMissing = true)
public class FileServiceImpl extends ServiceImpl<FileMapper, SysFile> implements FileService {

    private final FileConverter fileConverter;
    private final FileMapper fileMapper;
    private final FileUploadSecurityService fileUploadSecurityService;
    private final FileStorageAdapter storageAdapter;

    @Value("${file.local.public-url}")
    private String publicUrl;

    @Override
    public IPage<FileVO> getFilePage(FileQuery queryParams) {
        Page<FileVO> pageVO = this.baseMapper.getFilePage(queryParams.buildPage(), queryParams);
        return pageVO;
    }

    @Override
    public FileForm getFileFormData(Long id) {
        SysFile entity = this.getById(id);
        return fileConverter.toForm(entity);
    }

    @Override
    public boolean saveFile(FileForm formData) {
        SysFile entity = fileConverter.toEntity(formData);
        return this.save(entity);
    }

    @Override
    public boolean updateFile(Long id, FileForm formData) {
        SysFile entity = fileConverter.toEntity(formData);
        return this.updateById(entity);
    }

    @Override
    public boolean deleteFiles(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的文件数据不能为空");
        List<Long> idList = Arrays.stream(ids.split(","))
            .map(Long::parseLong)
            .toList();
        deleteBatch(idList);
        return true;
    }

    @Override
    public SysFile upload(MultipartFile file) throws Exception {
        return upload(file, "GENERAL");
    }

    @Override
    public SysFile upload(MultipartFile file, String bizType) throws Exception {
        fileUploadSecurityService.validateUpload(file);

        Long ownerId = SecurityUtils.getUserId();
        String actualBizType = StrUtil.blankToDefault(bizType, "GENERAL");
        String fileName = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(fileName);
        String hash = DigestUtils.md5DigestAsHex(file.getInputStream());

        SysFile exists = findByOwnerBizAndHash(ownerId, actualBizType, hash);
        if (exists != null && Integer.valueOf(1).equals(exists.getStatus())) {
            return exists;
        }

        String path = LocalDate.now().toString().replace("-", "/")
            + "/" + UUID.randomUUID() + "." + ext;

        save(file.getInputStream(), path, contentType(file.getContentType(), ext));

        SysFile entity = newFileEntity(ownerId, actualBizType, fileName, ext, file.getSize(), hash, path,
            contentType(file.getContentType(), ext));
        try {
            if (exists == null) {
                fileMapper.insert(entity);
            } else {
                entity.setId(exists.getId());
                entity.setUpdateTime(LocalDateTime.now());
                fileMapper.updateById(entity);
                deleteStaleStorage(exists.getStoragePath(), path);
            }
        } catch (DuplicateKeyException ex) {
            // 并发/重试上传可能在预查询后先完成插入。此处按幂等上传返回既有记录。
            SysFile concurrentFile = findByOwnerBizAndHash(ownerId, actualBizType, hash);
            if (concurrentFile != null) {
                try {
                    delete(path);
                } catch (IOException ignored) {
                }
                return concurrentFile;
            }
            throw ex;
        } catch (RuntimeException ex) {
            try {
                delete(path);
            } catch (IOException ignored) {
            }
            throw ex;
        }
        return entity;
    }

    private void deleteStaleStorage(String stalePath, String currentPath) {
        if (stalePath == null || stalePath.isBlank() || stalePath.equals(currentPath)) {
            return;
        }
        try {
            delete(stalePath);
        } catch (IOException ignored) {
        }
    }

    private SysFile findByOwnerBizAndHash(Long ownerId, String bizType, String hash) {
        return fileMapper.selectOne(new LambdaQueryWrapper<SysFile>()
            .eq(ownerId != null, SysFile::getOwnerId, ownerId)
            .isNull(ownerId == null, SysFile::getOwnerId)
            .eq(SysFile::getBizType, bizType)
            .eq(SysFile::getFileHash, hash));
    }

    private SysFile newFileEntity(Long ownerId, String bizType, String fileName, String ext, long fileSize,
                                  String hash, String path, String contentType) {
        SysFile entity = new SysFile();
        entity.setOwnerId(ownerId);
        entity.setBizType(bizType);
        entity.setFileName(fileName);
        entity.setContentType(contentType);
        entity.setFileExt(ext);
        entity.setFileSize(fileSize);
        entity.setFileHash(hash);
        entity.setHashAlgorithm("MD5");
        entity.setStorageType("LOCAL");
        entity.setStorageStatus("READY");
        entity.setStoragePath(path);
        entity.setUrl(publicUrl);
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }

    private String contentType(String contentType, String ext) {
        return contentType == null || contentType.isBlank() ? getContentTypeByExt(ext) : contentType;
    }

    private String getContentTypeByExt(String ext) {
        if (ext == null) {
            return "application/octet-stream";
        }

        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            case "bmp" -> "image/bmp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "csv" -> "text/csv";
            case "md" -> "text/markdown";
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "mkv" -> "video/x-matroska";
            case "flv" -> "video/x-flv";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "flac" -> "audio/flac";
            case "aac" -> "audio/aac";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "7z" -> "application/x-7z-compressed";
            case "tar" -> "application/x-tar";
            case "gz" -> "application/gzip";
            default -> "application/octet-stream";
        };
    }

    @Override
    public void download(Long id, HttpServletResponse response) throws Exception {
        writeFile(id, response, false);
    }

    @Override
    public void preview(Long id, HttpServletResponse response) throws Exception {
        writeFile(id, response, true);
    }

    private void writeFile(Long id, HttpServletResponse response, boolean inline) throws Exception {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null || !Integer.valueOf(1).equals(sysFile.getStatus())) {
            throw new BusinessException("文件不存在或已失效");
        }
        if (sysFile.getStoragePath() == null || sysFile.getStoragePath().isBlank()) {
            throw new BusinessException("文件存储路径为空");
        }

        String contentType = contentType(sysFile.getContentType(), sysFile.getFileExt());
        response.setContentType(contentType);
        response.setContentLengthLong(storageAdapter.size(sysFile.getStoragePath()));
        String fileName = fileNameWithExtension(sysFile);
        String disposition = inline ? "inline" : "attachment";
        response.setHeader("Content-Disposition", disposition + "; filename*=UTF-8''"
            + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        try (InputStream in = get(sysFile.getStoragePath()); OutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
        }
    }

    private String fileNameWithExtension(SysFile file) {
        String fileName = file.getFileName();
        if (fileName == null || fileName.isBlank()) {
            fileName = "download";
        }
        if (!fileName.contains(".") && file.getFileExt() != null && !file.getFileExt().isBlank()) {
            fileName += "." + file.getFileExt();
        }
        return fileName;
    }

    @Override
    public void delete(Long id) {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            return;
        }

        sysFile.setStatus(0);
        fileMapper.updateById(sysFile);

        try {
            delete(sysFile.getStoragePath());
        } catch (Exception ignored) {
        }
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<SysFile> sysFiles = fileMapper.selectBatchIds(ids);
        for (SysFile sysFile : sysFiles) {
            sysFile.setStatus(0);
            fileMapper.updateById(sysFile);
            try {
                delete(sysFile.getStoragePath());
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void uploadChunk(MultipartFile file, FileChunkUploadDTO dto) throws IOException {
        validateChunkRequest(dto);
        fileUploadSecurityService.validateDeclaredFile(dto.getFileName(), dto.getFileSize());
        fileUploadSecurityService.validateChunk(file, dto.getFileName());

        String chunkDir = "chunk/" + SecurityUtils.getUserId() + "/" + dto.getFileHash();
        String chunkPath = chunkDir + "/" + dto.getChunkIndex();
        save(file.getInputStream(), chunkPath,
            contentType(file.getContentType(), FilenameUtils.getExtension(dto.getFileName())));
    }

    @Override
    @Transactional
    public SysFile mergeChunks(FileChunkUploadDTO dto) {
        validateChunkRequest(dto);
        fileUploadSecurityService.validateDeclaredFile(dto.getFileName(), dto.getFileSize());
        Long ownerId = SecurityUtils.getUserId();
        SysFile exists = fileMapper.selectOne(new LambdaQueryWrapper<SysFile>()
            .eq(ownerId != null, SysFile::getOwnerId, ownerId)
            .isNull(ownerId == null, SysFile::getOwnerId)
            .eq(SysFile::getBizType, "GENERAL")
            .eq(SysFile::getFileHash, dto.getFileHash())
            .eq(SysFile::getStatus, 1));
        if (exists != null) {
            cleanupChunks(dto);
            return exists;
        }

        String ext = FilenameUtils.getExtension(dto.getFileName());
        String finalPath = LocalDate.now().toString().replace("-", "/")
            + "/" + UUID.randomUUID() + "." + ext;

        File targetFile = null;
        boolean stored = false;
        try {
            targetFile = Files.createTempFile("hboot-merge-", "." + ext).toFile();
            long mergedSize = 0L;
            try (OutputStream out = Files.newOutputStream(targetFile.toPath())) {
            for (int i = 0; i < dto.getTotalChunks(); i++) {
                String chunkPath = "chunk/" + SecurityUtils.getUserId() + "/" + dto.getFileHash() + "/" + i;
                try (InputStream in = get(chunkPath)) {
                    IOUtils.copy(in, out);
                    mergedSize += storageAdapter.size(chunkPath);
                }
            }
            }
            if (mergedSize != dto.getFileSize()) {
                throw new BusinessException("分片合并后的文件大小与声明不一致");
            }

            fileUploadSecurityService.validateMergedFile(targetFile, dto.getFileName());
            String hashAlgorithm = dto.getFileHash().length() == 64 ? "SHA-256" : "MD5";
            String actualHash = hashOf(targetFile, hashAlgorithm);
            if (!dto.getFileHash().equalsIgnoreCase(actualHash)) {
                throw new BusinessException("文件 hash 校验失败");
            }
            try (InputStream merged = new FileInputStream(targetFile)) {
                storageAdapter.save(merged, finalPath, getContentTypeByExt(ext));
            }
            stored = true;

            SysFile sysFile = new SysFile();
            sysFile.setOwnerId(ownerId);
            sysFile.setBizType("GENERAL");
            sysFile.setFileName(dto.getFileName());
            sysFile.setFileExt(ext);
            sysFile.setContentType(getContentTypeByExt(ext));
            sysFile.setFileSize(targetFile.length());
            sysFile.setFileHash(dto.getFileHash().toLowerCase());
            sysFile.setHashAlgorithm(hashAlgorithm);
            sysFile.setStoragePath(finalPath);
            sysFile.setStorageType("LOCAL");
            sysFile.setStorageStatus("READY");
            sysFile.setStatus(1);
            sysFile.setUrl(publicUrl);
            sysFile.setCreateTime(LocalDateTime.now());
            fileMapper.insert(sysFile);
            cleanupChunks(dto);
            return sysFile;
        } catch (IOException e) {
            if (stored) {
                try {
                    delete(finalPath);
                } catch (IOException ignored) {
                }
            }
            throw new BusinessException("分片合并失败: " + e.getMessage());
        } catch (RuntimeException e) {
            if (stored) {
                try {
                    delete(finalPath);
                } catch (IOException ignored) {
                }
            }
            throw e;
        } finally {
            if (targetFile != null) {
                try {
                    Files.deleteIfExists(targetFile.toPath());
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void validateChunkRequest(FileChunkUploadDTO dto) {
        if (dto == null || dto.getFileHash() == null || dto.getFileName() == null
            || dto.getChunkIndex() == null || dto.getTotalChunks() == null || dto.getFileSize() == null) {
            throw new BusinessException("分片参数不完整");
        }
        if (dto.getTotalChunks() > 10000 || dto.getChunkIndex() < 0
            || dto.getChunkIndex() >= dto.getTotalChunks()) {
            throw new BusinessException("分片序号或总数不合法");
        }
        if (!dto.getFileHash().matches("(?i)^[a-f0-9]{32}([a-f0-9]{32})?$")) {
            throw new BusinessException("文件 hash 格式不合法");
        }
    }

    private String hashOf(File file, String algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (InputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    if (read > 0) digest.update(buffer, 0, read);
                }
            }
            StringBuilder result = new StringBuilder();
            for (byte item : digest.digest()) result.append(String.format("%02x", item));
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException("计算文件 hash 失败", e);
        }
    }

    private void cleanupChunks(FileChunkUploadDTO dto) {
        for (int i = 0; i < dto.getTotalChunks(); i++) {
            try {
                delete("chunk/" + SecurityUtils.getUserId() + "/" + dto.getFileHash() + "/" + i);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public String save(InputStream inputStream, String path, String contentType) throws IOException {
        return storageAdapter.save(inputStream, path, contentType);
    }

    @Override
    public InputStream get(String path) throws IOException {
        return storageAdapter.get(path);
    }

    @Override
    public void delete(String path) throws IOException {
        storageAdapter.delete(path);
    }
}
