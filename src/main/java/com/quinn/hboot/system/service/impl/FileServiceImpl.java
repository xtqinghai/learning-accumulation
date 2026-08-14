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
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage", havingValue = "local", matchIfMissing = true)
public class FileServiceImpl extends ServiceImpl<FileMapper, SysFile> implements FileService {

    private final FileConverter fileConverter;
    private final FileMapper fileMapper;
    private final FileUploadSecurityService fileUploadSecurityService;

    @Value("${file.local.public-url}")
    private String publicUrl;

    @Value("${file.local.base-path}")
    private String basePath;

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
        return this.removeByIds(idList);
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

        SysFile exists = fileMapper.selectOne(
            new LambdaQueryWrapper<SysFile>()
                .eq(ownerId != null, SysFile::getOwnerId, ownerId)
                .isNull(ownerId == null, SysFile::getOwnerId)
                .eq(SysFile::getBizType, actualBizType)
                .eq(SysFile::getFileHash, hash)
                .eq(SysFile::getStatus, 1)
        );
        if (exists != null) {
            return exists;
        }

        String path = LocalDate.now().toString().replace("-", "/")
            + "/" + UUID.randomUUID() + "." + ext;

        save(file.getInputStream(), path, file.getContentType());

        SysFile entity = new SysFile();
        entity.setOwnerId(ownerId);
        entity.setBizType(actualBizType);
        entity.setFileName(fileName);
        entity.setContentType(file.getContentType());
        entity.setFileExt(ext);
        entity.setFileSize(file.getSize());
        entity.setFileHash(hash);
        entity.setHashAlgorithm("MD5");
        entity.setStorageType("LOCAL");
        entity.setStorageStatus("READY");
        entity.setStoragePath(path);
        entity.setUrl(publicUrl);
        entity.setCreateTime(LocalDateTime.now());

        fileMapper.insert(entity);
        return entity;
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
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            return;
        }

        String contentType = sysFile.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = getContentTypeByExt(sysFile.getFileExt());
        }
        response.setContentType(contentType);

        String fileName = sysFile.getFileName();
        if (!fileName.contains(".") && sysFile.getFileExt() != null) {
            fileName = fileName + "." + sysFile.getFileExt();
        }

        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\""
        );

        try (InputStream in = get(sysFile.getStoragePath()); OutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
        }
    }

    @Override
    public void preview(Long id, HttpServletResponse response) throws Exception {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) {
            return;
        }

        String contentType = sysFile.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = getContentTypeByExt(sysFile.getFileExt());
        }
        response.setContentType(contentType);
        response.setHeader(
            "Content-Disposition",
            "inline; filename=\"" + URLEncoder.encode(sysFile.getFileName(), StandardCharsets.UTF_8) + "\""
        );

        try (InputStream in = get(sysFile.getStoragePath()); OutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
        }
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
        fileUploadSecurityService.validateDeclaredFile(dto.getFileName(), dto.getFileSize());
        fileUploadSecurityService.validateChunk(file, dto.getFileName());

        String chunkDir = "chunk/" + SecurityUtils.getUserId() + "/" + dto.getFileHash();
        String chunkPath = chunkDir + "/" + dto.getChunkIndex();
        save(file.getInputStream(), chunkPath, file.getContentType());
    }

    @Override
    @Transactional
    public SysFile mergeChunks(FileChunkUploadDTO dto) {
        fileUploadSecurityService.validateDeclaredFile(dto.getFileName(), dto.getFileSize());
        String ext = FilenameUtils.getExtension(dto.getFileName());
        String finalPath = LocalDate.now().toString().replace("-", "/")
            + "/" + UUID.randomUUID() + "." + ext;

        File targetFile = new File(basePath, finalPath);
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (OutputStream out = Files.newOutputStream(targetFile.toPath())) {
            for (int i = 0; i < dto.getTotalChunks(); i++) {
                String chunkPath = "chunk/" + SecurityUtils.getUserId() + "/" + dto.getFileHash() + "/" + i;
                try (InputStream in = get(chunkPath)) {
                    IOUtils.copy(in, out);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        fileUploadSecurityService.validateMergedFile(targetFile, dto.getFileName());

        SysFile sysFile = new SysFile();
        sysFile.setOwnerId(SecurityUtils.getUserId());
        sysFile.setBizType("GENERAL");
        sysFile.setFileName(dto.getFileName());
        sysFile.setFileExt(ext);
        sysFile.setContentType(getContentTypeByExt(ext));
        sysFile.setFileSize(targetFile.length());
        sysFile.setFileHash(dto.getFileHash());
        sysFile.setHashAlgorithm("MD5");
        sysFile.setStoragePath(finalPath);
        sysFile.setStorageType("LOCAL");
        sysFile.setStorageStatus("READY");
        sysFile.setUrl(publicUrl);
        sysFile.setCreateTime(LocalDateTime.now());
        fileMapper.insert(sysFile);

        return sysFile;
    }

    @Override
    public String save(InputStream inputStream, String path, String contentType) throws IOException {
        File file = new File(basePath, path);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return path;
    }

    @Override
    public InputStream get(String path) throws IOException {
        return Files.newInputStream(new File(basePath, path).toPath());
    }

    @Override
    public void delete(String path) throws IOException {
        Files.deleteIfExists(new File(basePath, path).toPath());
    }
}
