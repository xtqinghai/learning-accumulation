package com.quinn.hboot.knowledge.service.impl;

import com.quinn.hboot.knowledge.mapper.KnowledgeArticleMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleChunkMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleVersionMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeImportTaskMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeSourceFileMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleVersion;
import com.quinn.hboot.knowledge.model.entity.KnowledgeImportTask;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSourceFile;
import com.quinn.hboot.knowledge.support.KnowledgeTextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeImportFinalizer {
    private final KnowledgeArticleMapper articleMapper;
    private final KnowledgeArticleChunkMapper articleChunkMapper;
    private final KnowledgeArticleVersionMapper versionMapper;
    private final KnowledgeSourceFileMapper sourceFileMapper;
    private final KnowledgeImportTaskMapper taskMapper;

    @Transactional
    public Long saveArticle(Long taskId, String title, String markdown, String model) {
        KnowledgeImportTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalStateException("导入任务不存在");
        }
        if (task.getArticleId() != null) {
            return task.getArticleId();
        }
        KnowledgeSourceFile source = sourceFileMapper.selectById(task.getSourceFileId());
        if (source == null) {
            throw new IllegalStateException("导入文件不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        KnowledgeArticle article = new KnowledgeArticle();
        article.setSpaceId(source.getSpaceId());
        article.setOwnerId(source.getOwnerId());
        article.setTitle(limit(title, 200));
        article.setSummary(limit(buildSummary(markdown, source.getFileName()), 2000));
        article.setContent(markdown);
        article.setSearchContent(KnowledgeTextUtils.toSearchText(markdown));
        article.setStatus("DRAFT");
        article.setSourceType("AI_SUMMARY");
        article.setSourceFileId(source.getId());
        article.setCreateTime(now);
        article.setUpdateTime(now);
        article.setDeleted(0);
        articleMapper.insert(article);

        KnowledgeArticleVersion version = new KnowledgeArticleVersion();
        version.setArticleId(article.getId());
        version.setVersionNo(1);
        version.setTitle(article.getTitle());
        version.setSummary(article.getSummary());
        version.setContent(markdown);
        version.setContentHash(sha256(markdown));
        version.setSource("AI_GENERATED");
        version.setCreatedBy(source.getOwnerId());
        version.setCreateTime(now);
        versionMapper.insert(version);

        article.setCurrentVersionId(version.getId());
        articleMapper.updateById(article);
        syncArticleChunks(article);

        source.setArticleId(article.getId());
        source.setStatus("READY");
        source.setErrorMessage(null);
        source.setUpdateTime(now);
        sourceFileMapper.updateById(source);

        task.setArticleId(article.getId());
        task.setStatus("READY");
        task.setProgress(100);
        task.setCurrentStage("总结文章已生成");
        task.setErrorMessage(null);
        task.setModel(model);
        task.setUpdateTime(now);
        taskMapper.updateById(task);
        return article.getId();
    }

    private String buildSummary(String markdown, String fileName) {
        String plain = KnowledgeTextUtils.toSearchText(markdown);
        if (plain.isBlank()) {
            return "由原始文件“" + fileName + "”生成的 AI 总结。";
        }
        return plain.substring(0, Math.min(500, plain.length()));
    }

    private void syncArticleChunks(KnowledgeArticle article) {
        articleChunkMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<KnowledgeArticleChunk>()
            .eq(KnowledgeArticleChunk::getArticleId, article.getId()));
        LocalDateTime now = LocalDateTime.now();
        List<KnowledgeTextUtils.Chunk> chunks = KnowledgeTextUtils.splitIntoChunks(
            article.getContent(),
            1200,
            150
        );
        for (KnowledgeTextUtils.Chunk chunk : chunks) {
            KnowledgeArticleChunk record = new KnowledgeArticleChunk();
            record.setArticleId(article.getId());
            record.setSpaceId(article.getSpaceId());
            record.setChunkNo(chunk.chunkNo());
            record.setContent(chunk.content());
            record.setSearchContent(chunk.searchContent());
            record.setCharStart(chunk.charStart());
            record.setCharEnd(chunk.charEnd());
            record.setCreateTime(now);
            record.setUpdateTime(now);
            articleChunkMapper.insert(record);
        }
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "未命名文档";
        }
        String trimmed = value.trim();
        return trimmed.substring(0, Math.min(maxLength, trimmed.length()));
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("无法计算文章内容摘要", ex);
        }
    }
}
