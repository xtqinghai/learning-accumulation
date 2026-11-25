package com.quinn.hboot.ai.config;

import com.quinn.hboot.knowledge.config.KnowledgeEmbeddingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AiProperties.class, KnowledgeEmbeddingProperties.class})
public class AiConfiguration {
}
