package com.quinn.hboot.knowledge.service.impl;

import com.quinn.hboot.knowledge.service.KnowledgeImportEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class KnowledgeImportEventListener {
    private final KnowledgeImportProcessor processor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(KnowledgeImportEvent event) {
        processor.processAsync(event.taskId());
    }
}
