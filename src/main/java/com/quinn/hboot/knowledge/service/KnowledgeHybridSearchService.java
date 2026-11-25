package com.quinn.hboot.knowledge.service;

import com.quinn.hboot.knowledge.config.KnowledgeEmbeddingProperties;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleChunkMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import com.quinn.hboot.knowledge.support.KnowledgeTextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeHybridSearchService {
    private static final int RRF_K = 60;
    private static final double KEYWORD_WEIGHT = 1.0;
    private static final double VECTOR_WEIGHT = 1.15;

    private final KnowledgeArticleChunkMapper chunkMapper;
    private final KnowledgeEmbeddingService embeddingService;
    private final KnowledgeEmbeddingProperties properties;

    public List<KnowledgeArticleChunk> search(Long spaceId, String question, String status, int limit) {
        int safeLimit = Math.max(1, limit);
        int candidateLimit = Math.max(safeLimit, properties.candidateLimit());
        List<String> tokens = KnowledgeTextUtils.buildSearchTokens(question);
        List<KnowledgeArticleChunk> keywordChunks = chunkMapper.searchRelevant(
            spaceId,
            tokens,
            status,
            candidateLimit
        );

        List<KnowledgeArticleChunk> vectorChunks = searchVector(spaceId, question, status, candidateLimit);
        if (vectorChunks.isEmpty()) {
            return keywordChunks.stream().limit(safeLimit).toList();
        }

        Map<Long, ScoredChunk> merged = new LinkedHashMap<>();
        accumulate(merged, keywordChunks, KEYWORD_WEIGHT);
        accumulate(merged, vectorChunks, VECTOR_WEIGHT);
        return merged.values().stream()
            .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed()
                .thenComparing(item -> item.chunk().getId(), Comparator.reverseOrder()))
            .limit(safeLimit)
            .map(ScoredChunk::chunk)
            .toList();
    }

    private List<KnowledgeArticleChunk> searchVector(
        Long spaceId,
        String question,
        String status,
        int candidateLimit
    ) {
        if (!embeddingService.isEnabled()) {
            return List.of();
        }
        float[] queryEmbedding = embeddingService.embedQuery(question);
        String embedding = embeddingService.toVectorLiteral(queryEmbedding);
        if (embedding == null) {
            return List.of();
        }
        try {
            return chunkMapper.searchVector(spaceId, embedding, status, candidateLimit);
        } catch (DataAccessException ex) {
            log.warn("Vector search failed; using keyword results only. Check pgvector and the embedding column: {}",
                ex.getMostSpecificCause().getMessage());
            return List.of();
        }
    }

    private void accumulate(
        Map<Long, ScoredChunk> merged,
        List<KnowledgeArticleChunk> chunks,
        double weight
    ) {
        for (int rank = 0; rank < chunks.size(); rank++) {
            KnowledgeArticleChunk chunk = chunks.get(rank);
            double score = weight / (RRF_K + rank + 1.0);
            merged.compute(chunk.getId(), (id, current) -> current == null
                ? new ScoredChunk(chunk, score)
                : new ScoredChunk(current.chunk(), current.score() + score));
        }
    }

    private record ScoredChunk(KnowledgeArticleChunk chunk, double score) {
    }
}
