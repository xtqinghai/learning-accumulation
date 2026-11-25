package com.quinn.hboot.knowledge.service;

import com.quinn.hboot.knowledge.config.KnowledgeEmbeddingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeEmbeddingService {
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final KnowledgeEmbeddingProperties properties;
    private final AtomicBoolean unavailableLogged = new AtomicBoolean(false);

    public boolean isEnabled() {
        return properties.enabled();
    }

    /**
     * Returns an empty list when embeddings are disabled or the model is unavailable. Callers must
     * keep keyword search available in that case instead of failing the whole knowledge workflow.
     */
    public List<float[]> embed(List<String> texts) {
        if (!properties.enabled() || texts == null || texts.isEmpty()) {
            return List.of();
        }
        EmbeddingModel model = embeddingModelProvider.getIfAvailable();
        if (model == null) {
            logUnavailableOnce("embedding model bean is not available");
            return List.of();
        }

        List<String> normalized = texts.stream()
            .map(text -> StringUtils.hasText(text) ? text.trim() : " ")
            .toList();
        List<float[]> embeddings = new ArrayList<>(normalized.size());
        try {
            for (int start = 0; start < normalized.size(); start += properties.batchSize()) {
                int end = Math.min(normalized.size(), start + properties.batchSize());
                List<float[]> batch = model.embed(normalized.subList(start, end));
                if (batch.size() != end - start) {
                    log.warn("Embedding response size mismatch: expected {}, received {}",
                        end - start, batch.size());
                    return List.of();
                }
                for (float[] embedding : batch) {
                    if (embedding == null || embedding.length != properties.dimensions()) {
                        log.warn("Embedding dimension mismatch: expected {}, received {}",
                            properties.dimensions(), embedding == null ? 0 : embedding.length);
                        return List.of();
                    }
                    embeddings.add(embedding);
                }
            }
            unavailableLogged.set(false);
            return embeddings;
        } catch (Exception ex) {
            log.warn("Knowledge embedding request failed; keyword retrieval remains available: {}",
                ex.getMessage());
            return List.of();
        }
    }

    public float[] embedQuery(String question) {
        List<float[]> embeddings = embed(List.of(question));
        return embeddings.isEmpty() ? null : embeddings.get(0);
    }

    public String toVectorLiteral(float[] embedding) {
        if (embedding == null || embedding.length != properties.dimensions()) {
            return null;
        }
        StringBuilder value = new StringBuilder(embedding.length * 12).append('[');
        for (int index = 0; index < embedding.length; index++) {
            if (index > 0) {
                value.append(',');
            }
            value.append(embedding[index]);
        }
        return value.append(']').toString();
    }

    private void logUnavailableOnce(String reason) {
        if (unavailableLogged.compareAndSet(false, true)) {
            log.warn("Knowledge vector search is unavailable ({}); falling back to keyword search", reason);
        }
    }
}
