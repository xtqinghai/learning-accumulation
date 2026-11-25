package com.quinn.hboot.knowledge.service;

import com.quinn.hboot.ai.config.AiProperties;
import com.quinn.hboot.knowledge.config.KnowledgeEmbeddingProperties;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleChunkMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KnowledgeEmbeddingBackfillServiceTest {
    private final KnowledgeArticleChunkMapper chunkMapper = mock(KnowledgeArticleChunkMapper.class);
    private final KnowledgeEmbeddingService embeddingService = mock(KnowledgeEmbeddingService.class);
    private final KnowledgeEmbeddingProperties properties =
        new KnowledgeEmbeddingProperties(true, 1024, 2, 20, true);
    private final AiProperties aiProperties =
        new AiProperties(true, "ollama", "qwen3.5:2b", "system prompt");
    private final KnowledgeEmbeddingBackfillService service =
        new KnowledgeEmbeddingBackfillService(chunkMapper, embeddingService, properties, aiProperties);

    @Test
    void indexesMissingChunksUntilNoRowsRemain() {
        KnowledgeArticleChunk first = chunk(1L, "第一段");
        KnowledgeArticleChunk second = chunk(2L, "第二段");
        float[] firstVector = new float[1024];
        float[] secondVector = new float[1024];
        firstVector[0] = 1.0f;
        secondVector[0] = 2.0f;

        when(embeddingService.isEnabled()).thenReturn(true);
        when(chunkMapper.findMissingEmbeddings(2))
            .thenReturn(List.of(first, second), List.of());
        when(embeddingService.embed(anyList())).thenReturn(List.of(firstVector, secondVector));
        when(embeddingService.toVectorLiteral(firstVector)).thenReturn("[1]");
        when(embeddingService.toVectorLiteral(secondVector)).thenReturn("[2]");
        when(chunkMapper.updateEmbedding(1L, "[1]")).thenReturn(1);
        when(chunkMapper.updateEmbedding(2L, "[2]")).thenReturn(1);

        service.backfillAsync();

        verify(chunkMapper).updateEmbedding(1L, "[1]");
        verify(chunkMapper).updateEmbedding(2L, "[2]");
    }

    @Test
    void skipsWorkWhenEmbeddingIsDisabled() {
        when(embeddingService.isEnabled()).thenReturn(false);

        service.backfillAsync();

        verifyNoInteractions(chunkMapper);
        verify(embeddingService, never()).embed(anyList());
    }

    private KnowledgeArticleChunk chunk(Long id, String content) {
        KnowledgeArticleChunk chunk = new KnowledgeArticleChunk();
        chunk.setId(id);
        chunk.setContent(content);
        chunk.setEmbeddingText("标题\n" + content);
        return chunk;
    }
}
