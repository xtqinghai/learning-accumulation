package com.quinn.hboot.knowledge.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class KnowledgeRequests {
    private KnowledgeRequests() {
    }

    public record ArticleSave(
        @NotBlank(message = "文章标题不能为空")
        @Size(max = 200, message = "文章标题不能超过 200 个字符")
        String title,

        @Size(max = 1000, message = "关键词不能超过 1000 个字符")
        String keywords,

        @Size(max = 2000, message = "摘要不能超过 2000 个字符")
        String summary,

        @NotBlank(message = "文章正文不能为空")
        String content,

        @Pattern(regexp = "DRAFT|PUBLISHED", message = "文章状态只支持 DRAFT 或 PUBLISHED")
        String status
    ) {
    }

    public record AnnotationSave(
        @NotBlank(message = "批注内容不能为空")
        @Size(max = 10000, message = "批注内容不能超过 10000 个字符")
        String content,

        @Size(max = 5000, message = "引用原文不能超过 5000 个字符")
        String quotedText,

        @Size(max = 20, message = "锚点类型不能超过 20 个字符")
        String anchorType,

        @Min(value = 0, message = "起始位置不能小于 0")
        Integer anchorStart,

        @Min(value = 0, message = "结束位置不能小于 0")
        Integer anchorEnd,

        @Size(max = 100, message = "段落标识不能超过 100 个字符")
        String anchorBlockId,

        @Min(value = 0, message = "置顶状态不正确")
        @Max(value = 1, message = "置顶状态不正确")
        Integer pinned
    ) {
    }

    public record AnnotationUpdate(
        @NotBlank(message = "批注内容不能为空")
        @Size(max = 10000, message = "批注内容不能超过 10000 个字符")
        String content,

        @Min(value = 0, message = "置顶状态不正确")
        @Max(value = 1, message = "置顶状态不正确")
        Integer pinned
    ) {
    }

    public record AnnotationFromQa(
        @Size(max = 10000, message = "批注内容不能超过 10000 个字符")
        String content,

        @Size(max = 5000, message = "选中回答不能超过 5000 个字符")
        String selectedText,

        @Size(max = 5000, message = "引用原文不能超过 5000 个字符")
        String quotedText,

        @Min(value = 0, message = "置顶状态不正确")
        @Max(value = 1, message = "置顶状态不正确")
        Integer pinned
    ) {
    }

    public record ArticleQuestion(
        Long sessionId,

        @NotBlank(message = "问题不能为空")
        @Size(max = 4000, message = "问题不能超过 4000 个字符")
        String question
    ) {
    }
}
