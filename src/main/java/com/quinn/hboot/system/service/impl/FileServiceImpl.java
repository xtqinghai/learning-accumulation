package com.quinn.hboot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.system.model.dto.FileChunkUploadDTO;
import com.quinn.hboot.system.model.entity.SysFile;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.FileMapper;
import com.quinn.hboot.system.service.FileService;
import com.quinn.hboot.system.model.form.FileForm;
import com.quinn.hboot.system.model.query.FileQuery;
import com.quinn.hboot.system.model.vo.FileVO;
import com.quinn.hboot.system.converter.FileConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理服务实现类
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.storage", havingValue = "local", matchIfMissing = true)
public class FileServiceImpl extends ServiceImpl<FileMapper, SysFile> implements FileService {

    private final FileConverter fileConverter;

    private final FileMapper fileMapper;

    @Value("${file.local.public-url}")
    private String publicUrl;

    @Value("${file.local.base-path}")
    private String basePath;

    /**
    * 获取文件管理分页列表
    *
    * @param queryParams 查询参数
    * @return {@link IPage<FileVO>} 文件管理分页列表
    */
    @Override
    public IPage<FileVO> getFilePage(FileQuery queryParams) {
        Page<FileVO> pageVO = this.baseMapper.getFilePage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );
        return pageVO;
    }

    /**
     * 获取文件管理表单数据
     *
     * @param id 文件管理ID
     * @return 文件管理表单数据
     */
    @Override
    public FileForm getFileFormData(Long id) {
        SysFile entity = this.getById(id);
        return fileConverter.toForm(entity);
    }

    /**
     * 新增文件管理
     *
     * @param formData 文件管理表单对象
     * @return 是否新增成功
     */
    @Override
    public boolean saveFile(FileForm formData) {
        SysFile entity = fileConverter.toEntity(formData);
        return this.save(entity);
    }

    /**
     * 更新文件管理
     *
     * @param id   文件管理ID
     * @param formData 文件管理表单对象
     * @return 是否修改成功
     */
    @Override
    public boolean updateFile(Long id,FileForm formData) {
        SysFile entity = fileConverter.toEntity(formData);
        return this.updateById(entity);
    }

    /**
     * 删除文件管理
     *
     * @param ids 文件管理ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFiles(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的文件管理数据为空");
        // 逻辑删除
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        return this.removeByIds(idList);
    }


    @Override
    public SysFile upload(MultipartFile file) throws Exception {

        String fileName = file.getOriginalFilename();
        String ext = FilenameUtils.getExtension(fileName);
        String hash = DigestUtils.md5DigestAsHex(file.getInputStream());

        // 秒传判断
        SysFile exists = fileMapper.selectOne(
                new LambdaQueryWrapper<SysFile>()
                        .eq(SysFile::getFileHash, hash)
                        .eq(SysFile::getStatus, 1)
        );
        if (exists != null) {
            return exists;
        }

        // 存储路径：yyyy/MM/dd/uuid.ext
        String path = LocalDate.now().toString().replace("-", "/")
                + "/" + UUID.randomUUID() + "." + ext;

        save(file.getInputStream(), path, file.getContentType());

        SysFile entity = new SysFile();
        entity.setFileName(fileName);
        entity.setFileExt(ext);
        entity.setFileSize(file.getSize());
        entity.setFileHash(hash);
        entity.setStorageType("LOCAL");
        entity.setStoragePath(path);
        entity.setUrl(publicUrl + "/" + path);
        entity.setCreateTime(LocalDateTime.now());

        fileMapper.insert(entity);
        return entity;
    }

    @Override
    public void download(Long id, HttpServletResponse response) throws Exception {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) return;

        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(sysFile.getFileName(), "UTF-8") + "\"");

        try (InputStream in = get(sysFile.getStoragePath());
             OutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
        }
    }

    @Override
    public void preview(Long id, HttpServletResponse response) throws Exception {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) return;

        response.setHeader("Content-Disposition",
                "inline; filename=\"" + URLEncoder.encode(sysFile.getFileName(), "UTF-8") + "\"");
        response.setContentType(sysFile.getContentType());

        try (InputStream in = get(sysFile.getStoragePath());
             OutputStream out = response.getOutputStream()) {
            IOUtils.copy(in, out);
        }
    }

    @Override
    public void delete(Long id) {
        SysFile sysFile = fileMapper.selectById(id);
        if (sysFile == null) return;

        sysFile.setStatus(0);
        fileMapper.updateById(sysFile);

        // 是否立即删磁盘可选
        try {
            delete(sysFile.getStoragePath());
        } catch (Exception ignored) {}
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
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void uploadChunk(MultipartFile file, FileChunkUploadDTO dto) throws IOException {

        String chunkDir = "chunk/" + dto.getFileHash();
        String chunkPath = chunkDir + "/" + dto.getChunkIndex();

        save(file.getInputStream(), chunkPath, file.getContentType());
    }

    @Override
    @Transactional
    public SysFile mergeChunks(FileChunkUploadDTO dto) {

        String ext = FilenameUtils.getExtension(dto.getFileName());
        String finalPath = LocalDate.now().toString().replace("-", "/")
                + "/" + UUID.randomUUID() + "." + ext;

        try (OutputStream out = Files.newOutputStream(
                new java.io.File(basePath, finalPath).toPath())) {

            for (int i = 0; i < dto.getTotalChunks(); i++) {
                String chunkPath = "chunk/" + dto.getFileHash() + "/" + i;
                try (InputStream in = get(chunkPath)) {
                    IOUtils.copy(in, out);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 保存文件记录（同普通 upload）
        SysFile sysFile = new SysFile();
        sysFile.setFileName(dto.getFileName());
        sysFile.setFileHash(dto.getFileHash());
        sysFile.setStoragePath(finalPath);
        sysFile.setStorageType("LOCAL");
        sysFile.setCreateTime(LocalDateTime.now());
        fileMapper.insert(sysFile);

        return sysFile;
    }

    @Override
    public String save(InputStream inputStream, String path, String contentType) throws IOException {
        java.io.File file = new java.io.File(basePath, path);
        java.io.File parent = file.getParentFile();
        if (!parent.exists()) parent.mkdirs();

        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return path;
    }

    @Override
    public InputStream get(String path) throws IOException {
        return Files.newInputStream(new java.io.File(basePath, path).toPath());
    }

    @Override
    public void delete(String path) throws IOException {
        Files.deleteIfExists(new java.io.File(basePath, path).toPath());
    }
}
