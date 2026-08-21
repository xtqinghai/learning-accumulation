package com.quinn.hboot.knowledge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import com.quinn.hboot.knowledge.model.dto.KnowledgeRequests;
import com.quinn.hboot.knowledge.model.entity.KnowledgeAnnotation;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticleVersion;
import com.quinn.hboot.knowledge.model.entity.KnowledgeImportTask;
import com.quinn.hboot.knowledge.model.entity.KnowledgeSpace;
import com.quinn.hboot.knowledge.model.vo.KnowledgeViews;
import com.quinn.hboot.knowledge.service.KnowledgeImportService;
import com.quinn.hboot.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "个人知识库")
@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {
    private final KnowledgeService knowledgeService;
    private final KnowledgeImportService knowledgeImportService;

    @Operation(summary = "获取当前用户知识空间")
    @GetMapping("/space")
    public R<KnowledgeSpace> getSpace() {
        return R.ok(knowledgeService.getMySpace());
    }

    @Operation(summary = "模糊搜索知识库文章")
    @GetMapping("/articles")
    public R<IPage<KnowledgeArticle>> searchArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1") long page,
        @RequestParam(defaultValue = "20") long size
    ) {
        return R.ok(knowledgeService.searchArticles(keyword, status, page, size));
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/articles/{id}")
    public R<KnowledgeArticle> getArticle(@PathVariable Long id) {
        return R.ok(knowledgeService.getArticle(id));
    }

    @Operation(summary = "新建 Markdown 文章")
    @PostMapping("/articles")
    public R<KnowledgeArticle> createArticle(@RequestBody @Valid KnowledgeRequests.ArticleSave request) {
        return R.ok(knowledgeService.createArticle(request));
    }

    @Operation(summary = "保存 Markdown 文章并生成新版本")
    @PutMapping("/articles/{id}")
    public R<KnowledgeArticle> updateArticle(
        @PathVariable Long id,
        @RequestBody @Valid KnowledgeRequests.ArticleSave request
    ) {
        return R.ok(knowledgeService.updateArticle(id, request));
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/articles/{id}")
    public R<Void> deleteArticle(@PathVariable Long id) {
        knowledgeService.deleteArticle(id);
        return R.ok();
    }

    @Operation(summary = "查看文章历史版本")
    @GetMapping("/articles/{id}/versions")
    public R<List<KnowledgeArticleVersion>> listVersions(@PathVariable Long id) {
        return R.ok(knowledgeService.listVersions(id));
    }

    @Operation(summary = "回滚文章历史版本")
    @PostMapping("/articles/{id}/versions/{versionId}/rollback")
    public R<KnowledgeArticle> rollbackArticle(@PathVariable Long id, @PathVariable Long versionId) {
        return R.ok(knowledgeService.rollbackArticle(id, versionId));
    }

    @Operation(summary = "查看文章批注")
    @GetMapping("/articles/{id}/annotations")
    public R<List<KnowledgeAnnotation>> listAnnotations(
        @PathVariable Long id,
        @RequestParam(required = false) Long versionId
    ) {
        return R.ok(knowledgeService.listAnnotations(id, versionId));
    }

    @Operation(summary = "新增文章批注")
    @PostMapping("/articles/{id}/annotations")
    public R<KnowledgeAnnotation> createAnnotation(
        @PathVariable Long id,
        @RequestBody @Valid KnowledgeRequests.AnnotationSave request
    ) {
        return R.ok(knowledgeService.createAnnotation(id, request));
    }

    @Operation(summary = "修改文章批注")
    @PutMapping("/articles/{id}/annotations/{annotationId}")
    public R<KnowledgeAnnotation> updateAnnotation(
        @PathVariable Long id,
        @PathVariable Long annotationId,
        @RequestBody @Valid KnowledgeRequests.AnnotationUpdate request
    ) {
        return R.ok(knowledgeService.updateAnnotation(id, annotationId, request));
    }

    @Operation(summary = "删除文章批注")
    @DeleteMapping("/articles/{id}/annotations/{annotationId}")
    public R<Void> deleteAnnotation(@PathVariable Long id, @PathVariable Long annotationId) {
        knowledgeService.deleteAnnotation(id, annotationId);
        return R.ok();
    }

    @Operation(summary = "将大模型回答添加为文章批注")
    @PostMapping("/articles/{id}/annotations/from-qa/{messageId}")
    public R<KnowledgeAnnotation> createAnnotationFromQa(
        @PathVariable Long id,
        @PathVariable Long messageId,
        @RequestBody @Valid KnowledgeRequests.AnnotationFromQa request
    ) {
        return R.ok(knowledgeService.createAnnotationFromQa(id, messageId, request));
    }

    @Operation(summary = "针对当前文章向大模型提问")
    @PostMapping("/articles/{id}/qa")
    public R<KnowledgeViews.QaAnswer> askArticle(
        @PathVariable Long id,
        @RequestBody @Valid KnowledgeRequests.ArticleQuestion request
    ) {
        return R.ok(knowledgeService.askArticle(id, request));
    }

    @Operation(summary = "查看当前文章问答记录")
    @GetMapping("/articles/{id}/qa/{sessionId}")
    public R<KnowledgeViews.QaHistory> getQaHistory(@PathVariable Long id, @PathVariable Long sessionId) {
        return R.ok(knowledgeService.getQaHistory(id, sessionId));
    }

    @Operation(summary = "上传文件并创建异步 AI 总结任务")
    @PostMapping("/source-files")
    public R<KnowledgeImportTask> uploadSourceFile(@RequestPart("file") MultipartFile file) throws Exception {
        return R.ok(knowledgeImportService.upload(file));
    }

    @Operation(summary = "查看文件导入任务进度")
    @GetMapping("/import-tasks/{taskId}")
    public R<KnowledgeImportTask> getImportTask(@PathVariable Long taskId) {
        return R.ok(knowledgeImportService.getTask(taskId));
    }

    @Operation(summary = "重试失败的文件导入任务")
    @PostMapping("/import-tasks/{taskId}/retry")
    public R<KnowledgeImportTask> retryImportTask(@PathVariable Long taskId) {
        return R.ok(knowledgeImportService.retryTask(taskId));
    }

    @Operation(summary = "查看文章关联的原始文件信息")
    @GetMapping("/articles/{articleId}/source-file")
    public R<KnowledgeViews.SourceFileInfo> getArticleSourceFile(@PathVariable Long articleId) {
        return R.ok(knowledgeImportService.getArticleSourceFile(articleId));
    }

    @Operation(summary = "查看原始文件信息")
    @GetMapping("/source-files/{sourceFileId}")
    public R<KnowledgeViews.SourceFileInfo> getSourceFile(@PathVariable Long sourceFileId) {
        return R.ok(knowledgeImportService.getSourceFile(sourceFileId));
    }

    @Operation(summary = "预览原始文件提取文本")
    @GetMapping("/source-files/{sourceFileId}/text-preview")
    public R<KnowledgeViews.SourcePreview> previewSourceText(@PathVariable Long sourceFileId) {
        return R.ok(knowledgeImportService.getSourcePreview(sourceFileId));
    }

    @Operation(summary = "浏览器内预览原始文件")
    @GetMapping("/source-files/{sourceFileId}/preview")
    public void previewSourceFile(
        @PathVariable Long sourceFileId,
        HttpServletResponse response
    ) throws Exception {
        knowledgeImportService.writeSourceFile(sourceFileId, response, false);
    }

    @Operation(summary = "下载原始文件")
    @GetMapping("/source-files/{sourceFileId}/download")
    public void downloadSourceFile(
        @PathVariable Long sourceFileId,
        HttpServletResponse response
    ) throws Exception {
        knowledgeImportService.writeSourceFile(sourceFileId, response, true);
    }
}
