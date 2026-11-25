package com.quinn.hboot.knowledge.config;

import com.quinn.hboot.knowledge.service.KnowledgeEmbeddingBackfillService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KnowledgeEmbeddingBackfillRunner implements ApplicationRunner {
    private final KnowledgeEmbeddingBackfillService backfillService;

    @Override
    public void run(ApplicationArguments args) {
        backfillService.backfillAsync();
    }
}
