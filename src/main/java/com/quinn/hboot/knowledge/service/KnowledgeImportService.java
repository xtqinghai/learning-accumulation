package com.quinn.hboot.knowledge.service;

import com.quinn.hboot.knowledge.model.entity.KnowledgeImportTask;
import com.quinn.hboot.knowledge.model.vo.KnowledgeViews;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeImportService {
    KnowledgeImportTask upload(MultipartFile file) throws Exception;

    KnowledgeImportTask getTask(Long taskId);

    KnowledgeImportTask retryTask(Long taskId);

    KnowledgeViews.SourceFileInfo getSourceFile(Long sourceFileId);

    KnowledgeViews.SourceFileInfo getArticleSourceFile(Long articleId);

    KnowledgeViews.SourcePreview getSourcePreview(Long sourceFileId);

    void writeSourceFile(Long sourceFileId, HttpServletResponse response, boolean download) throws Exception;
}
