package com.quinn.hboot.knowledge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.knowledge.embedding")
public record KnowledgeEmbeddingProperties(
    boolean enabled,
    int dimensions,
    int batchSize,
    int candidateLimit,
    boolean backfillEnabled
) {
    public KnowledgeEmbeddingProperties {
        dimensions = dimensions > 0 ? dimensions : 1024;
        batchSize = batchSize > 0 ? batchSize : 16;
        candidateLimit = candidateLimit > 0 ? candidateLimit : 24;
    }
}
