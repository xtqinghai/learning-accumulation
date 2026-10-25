package com.quinn.hboot.productivity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.knowledge.mapper.KnowledgeArticleMapper;
import com.quinn.hboot.knowledge.model.dto.KnowledgeRequests;
import com.quinn.hboot.knowledge.model.entity.KnowledgeArticle;
import com.quinn.hboot.knowledge.service.KnowledgeService;
import com.quinn.hboot.productivity.mapper.ArticleMetaMapper;
import com.quinn.hboot.productivity.mapper.ArticleRelationMapper;
import com.quinn.hboot.productivity.mapper.ArticleTagMapper;
import com.quinn.hboot.productivity.mapper.DevSnippetMapper;
import com.quinn.hboot.productivity.mapper.PersonalTaskMapper;
import com.quinn.hboot.productivity.mapper.ProductivityCategoryMapper;
import com.quinn.hboot.productivity.mapper.ProductivityTagMapper;
import com.quinn.hboot.productivity.mapper.ProjectDocumentMapper;
import com.quinn.hboot.productivity.mapper.TransferRecordMapper;
import com.quinn.hboot.productivity.model.dto.ProductivityRequests;
import com.quinn.hboot.productivity.model.entity.ArticleMeta;
import com.quinn.hboot.productivity.model.entity.ArticleRelation;
import com.quinn.hboot.productivity.model.entity.ArticleTag;
import com.quinn.hboot.productivity.model.entity.DevSnippet;
import com.quinn.hboot.productivity.model.entity.PersonalTask;
import com.quinn.hboot.productivity.model.entity.ProductivityCategory;
import com.quinn.hboot.productivity.model.entity.ProductivityTag;
import com.quinn.hboot.productivity.model.entity.ProjectDocument;
import com.quinn.hboot.productivity.model.entity.TransferRecord;
import com.quinn.hboot.productivity.model.vo.ProductivityOverview;
import com.quinn.hboot.schedule.mapper.ScheduledJobMapper;
import com.quinn.hboot.schedule.model.entity.ScheduledJob;
import com.quinn.hboot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductivityService {
    private final ProductivityCategoryMapper categoryMapper;
    private final ProductivityTagMapper tagMapper;
    private final DevSnippetMapper snippetMapper;
    private final ProjectDocumentMapper documentMapper;
    private final PersonalTaskMapper taskMapper;
    private final TransferRecordMapper transferMapper;
    private final KnowledgeArticleMapper articleMapper;
    private final ArticleMetaMapper articleMetaMapper;
    private final ArticleTagMapper articleTagMapper;
    private final ArticleRelationMapper articleRelationMapper;
    private final ScheduledJobMapper scheduledJobMapper;
    private final KnowledgeService knowledgeService;

    public List<ProductivityCategory> categories(String type) {
        return categoryMapper.selectList(new LambdaQueryWrapper<ProductivityCategory>()
            .eq(ProductivityCategory::getOwnerId, userId())
            .eq(StringUtils.hasText(type), ProductivityCategory::getType, type)
            .orderByAsc(ProductivityCategory::getSort)
            .orderByAsc(ProductivityCategory::getName));
    }

    public ProductivityCategory saveCategory(ProductivityRequests.CategorySave request) {
        ProductivityCategory category = new ProductivityCategory();
        category.setOwnerId(userId());
        category.setName(request.name().trim());
        category.setType(defaultValue(request.type(), "KNOWLEDGE"));
        category.setSort(defaultInt(request.sort(), 0));
        categoryMapper.insert(category);
        return category;
    }

    public void deleteCategory(Long id) {
        categoryMapper.delete(new LambdaQueryWrapper<ProductivityCategory>()
            .eq(ProductivityCategory::getId, id).eq(ProductivityCategory::getOwnerId, userId()));
    }

    public List<ProductivityTag> tags() {
        return tagMapper.selectList(new LambdaQueryWrapper<ProductivityTag>()
            .eq(ProductivityTag::getOwnerId, userId()).orderByAsc(ProductivityTag::getName));
    }

    public ProductivityTag saveTag(ProductivityRequests.TagSave request) {
        ProductivityTag tag = new ProductivityTag();
        tag.setOwnerId(userId());
        tag.setName(request.name().trim());
        tag.setColor(request.color());
        tagMapper.insert(tag);
        return tag;
    }

    public void deleteTag(Long id) {
        Long ownerId = userId();
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, id).eq(ArticleTag::getOwnerId, ownerId));
        tagMapper.delete(new LambdaQueryWrapper<ProductivityTag>().eq(ProductivityTag::getId, id).eq(ProductivityTag::getOwnerId, ownerId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveArticleMeta(Long articleId, ProductivityRequests.ArticleMetaSave request) {
        Long ownerId = userId();
        KnowledgeArticle article = articleMapper.selectById(articleId);
        if (article == null || !ownerId.equals(article.getOwnerId())) throw new BusinessException("文章不存在或无权访问");
        ArticleMeta meta = new ArticleMeta();
        ensureCategory(request.categoryId(), ownerId);
        meta.setArticleId(articleId); meta.setOwnerId(ownerId); meta.setCategoryId(request.categoryId());
        articleMetaMapper.deleteById(articleId);
        articleMetaMapper.insert(meta);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId).eq(ArticleTag::getOwnerId, ownerId));
        for (Long tagId : safeIds(request.tagIds())) {
            ensureTag(tagId, ownerId);
            ArticleTag articleTag = new ArticleTag(); articleTag.setArticleId(articleId); articleTag.setTagId(tagId); articleTag.setOwnerId(ownerId);
            articleTagMapper.insert(articleTag);
        }
        List<Long> previousRelatedIds = articleRelationMapper.selectList(new LambdaQueryWrapper<ArticleRelation>()
            .eq(ArticleRelation::getArticleId, articleId).eq(ArticleRelation::getOwnerId, ownerId))
            .stream().map(ArticleRelation::getRelatedArticleId).toList();
        articleRelationMapper.delete(new LambdaQueryWrapper<ArticleRelation>().eq(ArticleRelation::getArticleId, articleId).eq(ArticleRelation::getOwnerId, ownerId));
        if (!previousRelatedIds.isEmpty()) {
            articleRelationMapper.delete(new LambdaQueryWrapper<ArticleRelation>()
                .eq(ArticleRelation::getRelatedArticleId, articleId)
                .eq(ArticleRelation::getOwnerId, ownerId)
                .in(ArticleRelation::getArticleId, previousRelatedIds));
        }
        for (Long relatedId : safeIds(request.relatedArticleIds())) {
            if (articleId.equals(relatedId)) continue;
            KnowledgeArticle related = articleMapper.selectById(relatedId);
            if (related == null || !ownerId.equals(related.getOwnerId())) continue;
            addRelation(articleId, relatedId, ownerId);
            addRelation(relatedId, articleId, ownerId);
        }
    }

    public ArticleMeta articleMeta(Long articleId) {
        return articleMetaMapper.selectOne(new LambdaQueryWrapper<ArticleMeta>().eq(ArticleMeta::getArticleId, articleId).eq(ArticleMeta::getOwnerId, userId()));
    }

    public List<Long> articleTagIds(Long articleId) {
        return articleTagMapper.selectList(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId).eq(ArticleTag::getOwnerId, userId())).stream().map(ArticleTag::getTagId).toList();
    }

    public List<KnowledgeArticle> relatedArticles(Long articleId) {
        List<Long> ids = articleRelationMapper.selectList(new LambdaQueryWrapper<ArticleRelation>().eq(ArticleRelation::getArticleId, articleId).eq(ArticleRelation::getOwnerId, userId())).stream().map(ArticleRelation::getRelatedArticleId).toList();
        if (ids.isEmpty()) return List.of();
        return articleMapper.selectList(new LambdaQueryWrapper<KnowledgeArticle>().in(KnowledgeArticle::getId, ids).eq(KnowledgeArticle::getOwnerId, userId()));
    }

    public IPage<DevSnippet> snippets(String keyword, String language, long page, long size) {
        return snippetMapper.selectPage(new Page<>(page, boundedSize(size)), new LambdaQueryWrapper<DevSnippet>()
            .eq(DevSnippet::getOwnerId, userId()).eq(StringUtils.hasText(language), DevSnippet::getLanguage, language)
            .and(StringUtils.hasText(keyword), q -> q.like(DevSnippet::getTitle, keyword).or().like(DevSnippet::getTags, keyword).or().like(DevSnippet::getDescription, keyword).or().like(DevSnippet::getContent, keyword))
            .orderByDesc(DevSnippet::getUpdateTime));
    }

    public DevSnippet saveSnippet(Long id, ProductivityRequests.SnippetSave request) {
        DevSnippet snippet = id == null ? new DevSnippet() : ownSnippet(id);
        if (id == null) snippet.setOwnerId(userId());
        snippet.setTitle(request.title().trim()); snippet.setLanguage(request.language()); snippet.setCategoryId(request.categoryId());
        ensureCategory(request.categoryId(), userId());
        snippet.setContent(request.content()); snippet.setDescription(request.description()); snippet.setExample(request.example()); snippet.setTags(request.tags());
        if (id == null) snippetMapper.insert(snippet); else snippetMapper.updateById(snippet);
        return snippet;
    }

    public void deleteSnippet(Long id) { snippetMapper.delete(new LambdaQueryWrapper<DevSnippet>().eq(DevSnippet::getId, id).eq(DevSnippet::getOwnerId, userId())); }

    public IPage<ProjectDocument> documents(String keyword, String type, long page, long size) {
        return documentMapper.selectPage(new Page<>(page, boundedSize(size)), new LambdaQueryWrapper<ProjectDocument>()
            .eq(ProjectDocument::getOwnerId, userId()).eq(StringUtils.hasText(type), ProjectDocument::getDocumentType, type)
            .and(StringUtils.hasText(keyword), q -> q.like(ProjectDocument::getTitle, keyword).or().like(ProjectDocument::getProjectName, keyword).or().like(ProjectDocument::getTags, keyword).or().like(ProjectDocument::getContent, keyword))
            .orderByDesc(ProjectDocument::getUpdateTime));
    }

    public ProjectDocument saveDocument(Long id, ProductivityRequests.DocumentSave request) {
        ProjectDocument document = id == null ? new ProjectDocument() : ownDocument(id);
        if (id == null) document.setOwnerId(userId());
        document.setProjectName(request.projectName().trim()); document.setTitle(request.title().trim()); document.setDocumentType(request.documentType());
        ensureCategory(request.categoryId(), userId());
        document.setCategoryId(request.categoryId()); document.setContent(request.content()); document.setTags(request.tags()); document.setStatus(defaultValue(request.status(), "DRAFT"));
        if (id == null) documentMapper.insert(document); else documentMapper.updateById(document);
        return document;
    }

    public void deleteDocument(Long id) { documentMapper.delete(new LambdaQueryWrapper<ProjectDocument>().eq(ProjectDocument::getId, id).eq(ProjectDocument::getOwnerId, userId())); }

    public IPage<PersonalTask> tasks(String status, LocalDate date, long page, long size) {
        return taskMapper.selectPage(new Page<>(page, boundedSize(size)), new LambdaQueryWrapper<PersonalTask>()
            .eq(PersonalTask::getOwnerId, userId()).eq(StringUtils.hasText(status), PersonalTask::getStatus, status)
            .eq(Objects.nonNull(date), PersonalTask::getPlanDate, date)
            .orderByAsc(PersonalTask::getPlanDate).orderByAsc(PersonalTask::getDueTime).orderByDesc(PersonalTask::getPriority));
    }

    public PersonalTask saveTask(Long id, ProductivityRequests.TaskSave request) {
        PersonalTask task = id == null ? new PersonalTask() : ownTask(id);
        if (id == null) task.setOwnerId(userId());
        task.setTitle(request.title().trim()); task.setDescription(request.description()); task.setStatus(defaultValue(request.status(), "TODO"));
        task.setPriority(defaultValue(request.priority(), "MEDIUM")); task.setProgress(defaultInt(request.progress(), "DONE".equals(task.getStatus()) ? 100 : 0));
        task.setPlanDate(request.planDate()); task.setDueTime(request.dueTime()); task.setRecurrenceRule(request.recurrenceRule());
        task.setCompletedTime("DONE".equals(task.getStatus()) ? LocalDateTime.now() : null);
        if (id == null) taskMapper.insert(task); else taskMapper.updateById(task);
        return task;
    }

    public PersonalTask updateTaskStatus(Long id, ProductivityRequests.TaskStatusSave request) {
        PersonalTask task = ownTask(id); task.setStatus(request.status()); task.setProgress(request.progress());
        task.setCompletedTime("DONE".equals(request.status()) ? LocalDateTime.now() : null); taskMapper.updateById(task); return task;
    }

    public void deleteTask(Long id) { taskMapper.delete(new LambdaQueryWrapper<PersonalTask>().eq(PersonalTask::getId, id).eq(PersonalTask::getOwnerId, userId())); }

    public List<TransferRecord> transfers() {
        return transferMapper.selectList(new LambdaQueryWrapper<TransferRecord>().eq(TransferRecord::getOwnerId, userId()).orderByDesc(TransferRecord::getCreateTime).last("LIMIT 50"));
    }

    public TransferRecord createTransfer(String direction, String dataType, String format, String payload) {
        TransferRecord record = new TransferRecord(); record.setOwnerId(userId()); record.setDirection(direction); record.setDataType(dataType); record.setFormat(format); record.setStatus("PENDING"); record.setPayload(payload); record.setTotalCount(0); record.setSuccessCount(0); record.setFailedCount(0); transferMapper.insert(record); return record;
    }

    public TransferRecord finishTransfer(TransferRecord record, int total, int success, String error) {
        record.setTotalCount(total); record.setSuccessCount(success); record.setFailedCount(Math.max(total - success, 0)); record.setErrorMessage(error);
        record.setStatus(StringUtils.hasText(error) ? "FAILED" : "SUCCESS"); transferMapper.updateById(record); return record;
    }

    public TransferRecord transfer(Long id) { return ownTransfer(id); }

    public List<?> exportRows(String dataType) {
        Long ownerId = userId();
        return switch (dataType) {
            case "KNOWLEDGE" -> articleMapper.selectList(new LambdaQueryWrapper<KnowledgeArticle>().eq(KnowledgeArticle::getOwnerId, ownerId).orderByDesc(KnowledgeArticle::getUpdateTime));
            case "SNIPPET" -> snippetMapper.selectList(new LambdaQueryWrapper<DevSnippet>().eq(DevSnippet::getOwnerId, ownerId).orderByDesc(DevSnippet::getUpdateTime));
            case "DOCUMENT" -> documentMapper.selectList(new LambdaQueryWrapper<ProjectDocument>().eq(ProjectDocument::getOwnerId, ownerId).orderByDesc(ProjectDocument::getUpdateTime));
            case "TASK" -> taskMapper.selectList(new LambdaQueryWrapper<PersonalTask>().eq(PersonalTask::getOwnerId, ownerId).orderByDesc(PersonalTask::getUpdateTime));
            default -> throw new BusinessException("暂不支持该数据类型的导出");
        };
    }

    public ProductivityOverview overview() {
        Long ownerId = userId();
        List<KnowledgeArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<KnowledgeArticle>().eq(KnowledgeArticle::getOwnerId, ownerId).orderByDesc(KnowledgeArticle::getUpdateTime).last("LIMIT 6"));
        List<PersonalTask> tasks = taskMapper.selectList(new LambdaQueryWrapper<PersonalTask>().eq(PersonalTask::getOwnerId, ownerId).in(PersonalTask::getStatus, List.of("TODO", "IN_PROGRESS")).orderByAsc(PersonalTask::getPlanDate).last("LIMIT 8"));
        List<ScheduledJob> jobs = scheduledJobMapper.selectList(new LambdaQueryWrapper<ScheduledJob>().orderByDesc(ScheduledJob::getUpdateTime).last("LIMIT 6"));
        return new ProductivityOverview(articleMapper.selectCount(new LambdaQueryWrapper<KnowledgeArticle>().eq(KnowledgeArticle::getOwnerId, ownerId)), snippetMapper.selectCount(new LambdaQueryWrapper<DevSnippet>().eq(DevSnippet::getOwnerId, ownerId)), documentMapper.selectCount(new LambdaQueryWrapper<ProjectDocument>().eq(ProjectDocument::getOwnerId, ownerId)), taskMapper.selectCount(new LambdaQueryWrapper<PersonalTask>().eq(PersonalTask::getOwnerId, ownerId).in(PersonalTask::getStatus, List.of("TODO", "IN_PROGRESS"))), articles, tasks, jobs);
    }

    private void addRelation(Long articleId, Long relatedId, Long ownerId) {
        ArticleRelation relation = new ArticleRelation(); relation.setArticleId(articleId); relation.setRelatedArticleId(relatedId); relation.setOwnerId(ownerId);
        articleRelationMapper.insert(relation);
    }
    private void ensureTag(Long tagId, Long ownerId) { if (tagMapper.selectCount(new LambdaQueryWrapper<ProductivityTag>().eq(ProductivityTag::getId, tagId).eq(ProductivityTag::getOwnerId, ownerId)) == 0) throw new BusinessException("标签不存在"); }
    private void ensureCategory(Long categoryId, Long ownerId) { if (categoryId != null && categoryMapper.selectCount(new LambdaQueryWrapper<ProductivityCategory>().eq(ProductivityCategory::getId, categoryId).eq(ProductivityCategory::getOwnerId, ownerId)) == 0) throw new BusinessException("分类不存在"); }
    public KnowledgeArticle importKnowledge(String title, String content, String keywords, String summary, String status) { return knowledgeService.createArticle(new KnowledgeRequests.ArticleSave(title, keywords, summary, content, status)); }
    private DevSnippet ownSnippet(Long id) { DevSnippet v = snippetMapper.selectOne(new LambdaQueryWrapper<DevSnippet>().eq(DevSnippet::getId, id).eq(DevSnippet::getOwnerId, userId())); if (v == null) throw new BusinessException("代码片段不存在"); return v; }
    private ProjectDocument ownDocument(Long id) { ProjectDocument v = documentMapper.selectOne(new LambdaQueryWrapper<ProjectDocument>().eq(ProjectDocument::getId, id).eq(ProjectDocument::getOwnerId, userId())); if (v == null) throw new BusinessException("项目文档不存在"); return v; }
    private PersonalTask ownTask(Long id) { PersonalTask v = taskMapper.selectOne(new LambdaQueryWrapper<PersonalTask>().eq(PersonalTask::getId, id).eq(PersonalTask::getOwnerId, userId())); if (v == null) throw new BusinessException("任务不存在"); return v; }
    private TransferRecord ownTransfer(Long id) { TransferRecord v = transferMapper.selectOne(new LambdaQueryWrapper<TransferRecord>().eq(TransferRecord::getId, id).eq(TransferRecord::getOwnerId, userId())); if (v == null) throw new BusinessException("导入导出记录不存在"); return v; }
    private Long userId() { Long id = SecurityUtils.getUserId(); if (id == null) throw new BusinessException("登录状态已失效"); return id; }
    private List<Long> safeIds(Collection<Long> ids) { return ids == null ? Collections.emptyList() : ids.stream().filter(Objects::nonNull).distinct().toList(); }
    private String defaultValue(String value, String defaultValue) { return StringUtils.hasText(value) ? value : defaultValue; }
    private int defaultInt(Integer value, int defaultValue) { return value == null ? defaultValue : value; }
    private long boundedSize(long size) { return Math.min(Math.max(size, 1), 100); }
}
