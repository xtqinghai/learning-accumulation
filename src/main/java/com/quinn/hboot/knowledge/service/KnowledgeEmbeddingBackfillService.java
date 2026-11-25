package com.quinn.hboot.knowledge.service;

import com.quinn.hboot.ai.config.AiProperties;
import com.quinn.hboot.knowledge.config.KnowledgeEmbeddingProperties;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleChunkMapper;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeEmbeddingBackfillService {
    private final KnowledgeArticleChunkMapper chunkMapper;
    private final KnowledgeEmbeddingService embeddingService;
    private final KnowledgeEmbeddingProperties properties;
    private final AiProperties aiProperties;

    @Async("applicationTaskExecutor")
    public void backfillAsync() {
        if (!properties.backfillEnabled() || !embeddingService.isEnabled() || !aiProperties.isConfigured()) {
            return;
        }
        int indexed = 0;
        try {
            while (true) {
                List<KnowledgeArticleChunk> chunks = chunkMapper.findMissingEmbeddings(properties.batchSize());
                if (chunks.isEmpty()) {
                    break;
                }
                List<float[]> embeddings = embeddingService.embed(chunks.stream()
                    .map(chunk -> chunk.getEmbeddingText() == null
                        ? chunk.getContent()
                        : chunk.getEmbeddingText())
                    .toList());
                if (embeddings.size() != chunks.size()) {
                    log.warn("Embedding backfill stopped after {} chunks because the model is unavailable", indexed);
                    return;
                }
                int updated = 0;
                for (int index = 0; index < chunks.size(); index++) {
                    String embedding = embeddingService.toVectorLiteral(embeddings.get(index));
                    if (embedding == null) {
                        log.warn("Embedding backfill stopped after {} chunks due to a dimension mismatch", indexed);
                        return;
                    }
                    updated += chunkMapper.updateEmbedding(chunks.get(index).getId(), embedding);
                }
                indexed += updated;
                if (updated < chunks.size()) {
                    log.warn("Embedding backfill stopped after {} chunks because {} rows could not be updated",
                        indexed, chunks.size() - updated);
                    return;
                }
                if (chunks.size() < properties.batchSize()) {
                    break;
                }
            }
            log.info("Knowledge embedding backfill completed: {} chunks indexed", indexed);
        } catch (DataAccessException ex) {
            log.warn("Knowledge embedding backfill skipped. Ensure pgvector and migration 16 are installed: {}",
                ex.getMostSpecificCause().getMessage());
        } catch (Exception ex) {
            log.warn("Knowledge embedding backfill failed; keyword search remains available: {}", ex.getMessage(), ex);
        }
    }
}
