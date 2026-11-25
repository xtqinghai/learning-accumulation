package com.quinn.hboot.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.knowledge.mapper.KnowledgeImportTaskMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeSourceFileMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.knowledge.model.entity.KnowledgeImportTask;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSourceFile;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSpace;
import com.quinn.hboot.knowledge.model.vo.KnowledgeViews;
import com.quinn.hboot.knowledge.service.KnowledgeImportEvent;
import com.quinn.hboot.knowledge.service.KnowledgeImportService;
import com.quinn.hboot.knowledge.service.KnowledgeService;
import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.service.FileService;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KnowledgeImportServiceImpl implements KnowledgeImportService {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
        "md", "txt", "csv", "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx"
    );
    private static final int MAX_PREVIEW_CHARS = 200_000;

    private final KnowledgeImportTaskMapper taskMapper;
    private final KnowledgeSourceFileMapper sourceFileMapper;
    private final KnowledgeService knowledgeService;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public KnowledgeImportTask upload(MultipartFile file) throws Exception {
        String fileName = file == null ? null : file.getOriginalFilename();
        String extension = normalizeExtension(fileName);
        validateExtension(extension);

        Long userId = currentUserId();
        KnowledgeSpace space = knowledgeService.getMySpace();
        SysFile storedFile = fileService.upload(file, "KNOWLEDGE_SOURCE");
        LocalDateTime now = LocalDateTime.now();

        KnowledgeSourceFile source = new KnowledgeSourceFile();
        source.setSpaceId(space.getId());
        source.setOwnerId(userId);
        source.setSysFileId(storedFile.getId());
        source.setFileName(Objects.requireNonNullElse(storedFile.getFileName(), fileName));
        source.setFileExt(extension);
        source.setContentType(storedFile.getContentType());
        source.setFileSize(storedFile.getFileSize());
        source.setStatus("UPLOADED");
        source.setCreateTime(now);
        source.setUpdateTime(now);
        sourceFileMapper.insert(source);

        KnowledgeImportTask task = new KnowledgeImportTask();
        task.setSourceFileId(source.getId());
        task.setOwnerId(userId);
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setCurrentStage("等待解析");
        task.setRetryCount(0);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        taskMapper.insert(task);
        eventPublisher.publishEvent(new KnowledgeImportEvent(task.getId()));
        return task;
    }

    @Override
    public KnowledgeImportTask getTask(Long taskId) {
        return requireTask(taskId);
    }

    @Override
    @Transactional
    public KnowledgeImportTask retryTask(Long taskId) {
        KnowledgeImportTask task = requireTask(taskId);
        if (!"FAILED".equals(task.getStatus())) {
            throw new BusinessException("只有失败的导入任务可以重试");
        }
        task.setStatus("PENDING");
        task.setProgress(0);
        task.setCurrentStage("等待重试");
        task.setRetryCount(Objects.requireNonNullElse(task.getRetryCount(), 0) + 1);
        task.setErrorMessage(null);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        KnowledgeSourceFile source = requireSourceFile(task.getSourceFileId());
        source.setStatus("UPLOADED");
        source.setErrorMessage(null);
        source.setUpdateTime(LocalDateTime.now());
        sourceFileMapper.updateById(source);
        eventPublisher.publishEvent(new KnowledgeImportEvent(task.getId()));
        return task;
    }

    @Override
    public KnowledgeViews.SourceFileInfo getSourceFile(Long sourceFileId) {
        return toInfo(requireSourceFile(sourceFileId));
    }

    @Override
    public KnowledgeViews.SourceFileInfo getArticleSourceFile(Long articleId) {
        KnowledgeArticle article = knowledgeService.getArticle(articleId);
        if (article.getSourceFileId() == null) {
            throw new BusinessException("当前文章没有关联原始文件");
        }
        return getSourceFile(article.getSourceFileId());
    }

    @Override
    public KnowledgeViews.SourcePreview getSourcePreview(Long sourceFileId) {
        KnowledgeSourceFile source = requireSourceFile(sourceFileId);
        String text = Objects.requireNonNullElse(source.getParsedText(), "");
        boolean truncated = text.length() > MAX_PREVIEW_CHARS;
        return new KnowledgeViews.SourcePreview(
            source.getId(),
            source.getFileName(),
            truncated ? text.substring(0, MAX_PREVIEW_CHARS) : text,
            truncated,
            source.getStatus()
        );
    }

    @Override
    public void writeSourceFile(Long sourceFileId, HttpServletResponse response, boolean download) throws Exception {
        KnowledgeSourceFile source = requireSourceFile(sourceFileId);
        if (download) {
            fileService.download(source.getSysFileId(), response);
        } else {
            fileService.preview(source.getSysFileId(), response);
        }
    }

    private KnowledgeImportTask requireTask(Long taskId) {
        KnowledgeImportTask task = taskMapper.selectOne(new LambdaQueryWrapper<KnowledgeImportTask>()
            .eq(KnowledgeImportTask::getId, taskId)
            .eq(KnowledgeImportTask::getOwnerId, currentUserId()));
        if (task == null) {
            throw new BusinessException("导入任务不存在或无权访问");
        }
        return task;
    }

    private KnowledgeSourceFile requireSourceFile(Long sourceFileId) {
        KnowledgeSourceFile source = sourceFileMapper.selectOne(new LambdaQueryWrapper<KnowledgeSourceFile>()
            .eq(KnowledgeSourceFile::getId, sourceFileId)
            .eq(KnowledgeSourceFile::getOwnerId, currentUserId()));
        if (source == null) {
            throw new BusinessException("原始文件不存在或无权访问");
        }
        return source;
    }

    private KnowledgeViews.SourceFileInfo toInfo(KnowledgeSourceFile source) {
        return new KnowledgeViews.SourceFileInfo(
            source.getId(),
            source.getArticleId(),
            source.getFileName(),
            source.getFileExt(),
            source.getContentType(),
            source.getFileSize(),
            source.getStatus(),
            source.getErrorMessage(),
            source.getCreateTime()
        );
    }

    private void validateExtension(String extension) {
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 md、txt、csv、pdf、doc、docx、ppt、pptx、xls 和 xlsx 文件");
        }
    }

    private String normalizeExtension(String fileName) {
        return FilenameUtils.getExtension(Objects.requireNonNullElse(fileName, ""))
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private Long currentUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        return userId;
    }
}
