package com.quinn.hboot.knowledge.service;

import com.quinn.hboot.knowledge.config.KnowledgeEmbeddingProperties;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleChunkMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeHybridSearchServiceTest {
    private final KnowledgeArticleChunkMapper chunkMapper = mock(KnowledgeArticleChunkMapper.class);
    private final KnowledgeEmbeddingService embeddingService = mock(KnowledgeEmbeddingService.class);
    private final KnowledgeEmbeddingProperties properties =
        new KnowledgeEmbeddingProperties(true, 1024, 16, 20, true);
    private final KnowledgeHybridSearchService service =
        new KnowledgeHybridSearchService(chunkMapper, embeddingService, properties);

    @Test
    void fusesKeywordAndVectorCandidatesWithoutDuplicates() {
        KnowledgeArticleChunk keywordFirst = chunk(1L);
        KnowledgeArticleChunk keywordSecond = chunk(3L);
        KnowledgeArticleChunk vectorFirst = chunk(2L);

        when(chunkMapper.searchRelevant(eq(10L), anyList(), isNull(), eq(20)))
            .thenReturn(List.of(keywordFirst, keywordSecond));
        when(embeddingService.isEnabled()).thenReturn(true);
        when(embeddingService.embedQuery("如何配置知识库")).thenReturn(new float[1024]);
        when(embeddingService.toVectorLiteral(org.mockito.ArgumentMatchers.any(float[].class)))
            .thenReturn("[0.1,0.2]");
        when(chunkMapper.searchVector(eq(10L), eq("[0.1,0.2]"), isNull(), eq(20)))
            .thenReturn(List.of(keywordFirst, vectorFirst));

        List<KnowledgeArticleChunk> result = service.search(10L, "如何配置知识库", null, 3);

        assertEquals(List.of(1L, 2L, 3L), result.stream().map(KnowledgeArticleChunk::getId).toList());
    }

    @Test
    void fallsBackToKeywordSearchWhenVectorSearchIsDisabled() {
        KnowledgeArticleChunk keywordFirst = chunk(1L);
        when(chunkMapper.searchRelevant(eq(10L), anyList(), isNull(), eq(20)))
            .thenReturn(List.of(keywordFirst));
        when(embeddingService.isEnabled()).thenReturn(false);

        List<KnowledgeArticleChunk> result = service.search(10L, "问题", null, 6);

        assertEquals(List.of(keywordFirst), result);
        verify(chunkMapper, never()).searchVector(eq(10L), anyString(), isNull(), eq(20));
    }

    private KnowledgeArticleChunk chunk(Long id) {
        KnowledgeArticleChunk chunk = new KnowledgeArticleChunk();
        chunk.setId(id);
        return chunk;
    }
}
