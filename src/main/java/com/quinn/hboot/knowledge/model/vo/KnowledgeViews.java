package com.quinn.hboot.knowledge.model.vo;

import com.quinn.hboot.knowledge.model.entity.KnowledgeQaMessage;

import java.time.LocalDateTime;
import java.util.List;

public final class KnowledgeViews {
    private KnowledgeViews() {
    }

    public record QaAnswer(
        Long sessionId,
        Long messageId,
        String answer,
        String model
    ) {
    }

    public record QaHistory(
        Long sessionId,
        List<KnowledgeQaMessage> messages
    ) {
    }

    public record SourceFileInfo(
        Long id,
        Long articleId,
        String fileName,
        String fileExt,
        String contentType,
        Long fileSize,
        String status,
        String errorMessage,
        LocalDateTime createTime
    ) {
    }

    public record SourcePreview(
        Long id,
        String fileName,
        String text,
        boolean truncated,
        String status
    ) {
    }
}
