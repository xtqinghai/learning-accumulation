package com.quinn.hboot.knowledge.service.impl;

import com.quinn.hboot.knowledge.mapper.KnowledgeArticleChunkMapper;
import com.quinn.hboot.knowledge.mapper.KnowledgeSourceFileMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSourceFile;
import com.quinn.hboot.knowledge.service.KnowledgeEmbeddingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeChunkSynchronizerTest {

    @Mock
    private KnowledgeArticleChunkMapper chunkMapper;

    @Mock
    private KnowledgeSourceFileMapper sourceFileMapper;

    @Mock
    private KnowledgeEmbeddingService embeddingService;

    @InjectMocks
    private KnowledgeChunkSynchronizer synchronizer;

    @Test
    void importedArticleUsesOriginalParsedTextInsteadOfAiSummary() {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setId(1L);
        article.setSpaceId(2L);
        article.setSourceFileId(3L);
        article.setContent("这是 AI 生成的摘要，不应该进入检索切片。");

        KnowledgeSourceFile source = new KnowledgeSourceFile();
        source.setId(3L);
        source.setParsedText("这是原始文档正文。".repeat(100));

        when(sourceFileMapper.selectById(3L)).thenReturn(source);

        synchronizer.sync(article);

        List<KnowledgeArticleChunk> chunks = savedChunks();
        assertFalse(chunks.isEmpty());
        assertEquals(3L, chunks.get(0).getSourceFileId());
        assertTrue(chunks.get(0).getContent().startsWith("这是原始文档正文。"));
        assertFalse(chunks.get(0).getContent().contains("AI 生成的摘要"));
    }

    @Test
    void manualArticleFallsBackToItsOwnContent() {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setId(10L);
        article.setSpaceId(20L);
        article.setContent("手工文章正文。".repeat(100));

        synchronizer.sync(article);

        List<KnowledgeArticleChunk> chunks = savedChunks();
        assertFalse(chunks.isEmpty());
        assertNull(chunks.get(0).getSourceFileId());
        assertTrue(chunks.get(0).getContent().startsWith("手工文章正文。"));
    }

    private List<KnowledgeArticleChunk> savedChunks() {
        ArgumentCaptor<KnowledgeArticleChunk> captor = ArgumentCaptor.forClass(KnowledgeArticleChunk.class);
        verify(chunkMapper, atLeastOnce()).insert(captor.capture());
        return captor.getAllValues();
    }
}
