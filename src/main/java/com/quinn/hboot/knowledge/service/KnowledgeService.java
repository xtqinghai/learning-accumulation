package com.quinn.hboot.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.knowledge.model.dto.KnowledgeRequests;
import com.quinn.hboot.knowledge.model.entity.KnowledgeAnnotation;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleVersion;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSpace;
import com.quinn.hboot.knowledge.model.vo.KnowledgeViews;

import java.util.List;

public interface KnowledgeService {
    KnowledgeSpace getMySpace();

    IPage<KnowledgeArticle> searchArticles(String keyword, String status, long page, long size);

    KnowledgeArticle getArticle(Long id);

    KnowledgeArticle createArticle(KnowledgeRequests.ArticleSave request);

    KnowledgeArticle updateArticle(Long id, KnowledgeRequests.ArticleSave request);

    void deleteArticle(Long id);

    List<KnowledgeArticleVersion> listVersions(Long articleId);

    KnowledgeArticle rollbackArticle(Long articleId, Long versionId);

    List<KnowledgeAnnotation> listAnnotations(Long articleId, Long versionId);

    KnowledgeAnnotation createAnnotation(Long articleId, KnowledgeRequests.AnnotationSave request);

    KnowledgeAnnotation updateAnnotation(Long articleId, Long annotationId, KnowledgeRequests.AnnotationUpdate request);

    void deleteAnnotation(Long articleId, Long annotationId);

    KnowledgeAnnotation createAnnotationFromQa(
        Long articleId,
        Long messageId,
        KnowledgeRequests.AnnotationFromQa request
    );

    KnowledgeViews.QaAnswer askArticle(Long articleId, KnowledgeRequests.ArticleQuestion request);

    KnowledgeViews.QaAnswer askAll(KnowledgeRequests.ArticleQuestion request);

    KnowledgeViews.QaHistory getQaHistory(Long articleId, Long sessionId);
}
