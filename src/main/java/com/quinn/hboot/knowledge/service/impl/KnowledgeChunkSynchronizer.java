package com.quinn.hboot.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleChunkMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeSourceFileMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSourceFile;
import com.quinn.hboot.knowledge.service.KnowledgeEmbeddingService;
import com.quinn.hboot.knowledge.support.KnowledgeTextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Keeps the searchable chunk store aligned with the best available source text.
 *
 * <p>Imported articles are always chunked from the original parsed document. The generated
 * Markdown article remains a human-readable summary and no longer replaces the retrieval corpus
 * when the user edits it. Manually authored articles fall back to their own content.</p>
 */
@Service
@RequiredArgsConstructor
public class KnowledgeChunkSynchronizer {
    private static final int CHUNK_SIZE = 1_200;
    private static final int CHUNK_OVERLAP = 150;

    private final KnowledgeArticleChunkMapper chunkMapper;
    private final KnowledgeSourceFileMapper sourceFileMapper;
    private final KnowledgeEmbeddingService embeddingService;

    @Transactional
    public void deleteByArticleId(Long articleId) {
        chunkMapper.delete(new LambdaQueryWrapper<KnowledgeArticleChunk>()
            .eq(KnowledgeArticleChunk::getArticleId, articleId));
    }

    @Transactional
    public void sync(KnowledgeArticle article) {
        if (article == null || article.getId() == null) {
            throw new BusinessException("文章 ID 不能为空");
        }
        KnowledgeSourceFile source = article.getSourceFileId() == null
            ? null
            : sourceFileMapper.selectById(article.getSourceFileId());
        sync(article, source);
    }

    @Transactional
    public void sync(KnowledgeArticle article, KnowledgeSourceFile source) {
        if (article == null || article.getId() == null) {
            throw new BusinessException("文章 ID 不能为空");
        }
        String sourceText = source == null ? null : source.getParsedText();
        boolean useSource = StringUtils.hasText(sourceText);
        String chunkText = useSource ? sourceText : article.getContent();
        Long sourceFileId = useSource ? source.getId() : null;

        deleteByArticleId(article.getId());
        List<KnowledgeTextUtils.Chunk> chunks = KnowledgeTextUtils.splitIntoChunks(
            chunkText,
            CHUNK_SIZE,
            CHUNK_OVERLAP
        );
        List<float[]> embeddings = embeddingService.embed(chunks.stream()
            .map(chunk -> embeddingText(article.getTitle(), chunk.content()))
            .toList());
        LocalDateTime now = LocalDateTime.now();
        for (int index = 0; index < chunks.size(); index++) {
            KnowledgeTextUtils.Chunk chunk = chunks.get(index);
            KnowledgeArticleChunk record = new KnowledgeArticleChunk();
            record.setArticleId(article.getId());
            record.setSourceFileId(sourceFileId);
            record.setSpaceId(article.getSpaceId());
            record.setChunkNo(chunk.chunkNo());
            record.setContent(chunk.content());
            record.setSearchContent(chunk.searchContent());
            record.setCharStart(chunk.charStart());
            record.setCharEnd(chunk.charEnd());
            record.setCreateTime(now);
            record.setUpdateTime(now);
            chunkMapper.insert(record);
            if (embeddings.size() == chunks.size()) {
                String embedding = embeddingService.toVectorLiteral(embeddings.get(index));
                if (embedding != null) {
                    chunkMapper.updateEmbedding(record.getId(), embedding);
                }
            }
        }
    }

    private String embeddingText(String title, String content) {
        if (!StringUtils.hasText(title)) {
            return content;
        }
        return title.trim() + "\n" + content;
    }
}
