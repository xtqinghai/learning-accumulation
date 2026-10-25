package com.quinn.hboot.productivity.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ProductivityRequests {
    private ProductivityRequests() {
    }

    public record CategorySave(@NotBlank @Size(max = 100) String name, @Pattern(regexp = "KNOWLEDGE|SNIPPET|DOCUMENT") String type, @Min(0) Integer sort) {}
    public record TagSave(@NotBlank @Size(max = 60) String name, @Size(max = 20) String color) {}
    public record ArticleMetaSave(Long categoryId, List<Long> tagIds, List<Long> relatedArticleIds) {}
    public record SnippetSave(@NotBlank @Size(max = 200) String title, @NotBlank @Pattern(regexp = "Java|SQL|Shell|Vue|JavaScript|TypeScript|Other") String language, Long categoryId, @NotBlank String content, String description, String example, @Size(max = 1000) String tags) {}
    public record DocumentSave(@NotBlank @Size(max = 100) String projectName, @NotBlank @Size(max = 200) String title, @NotBlank @Pattern(regexp = "REQUIREMENT|TECHNICAL|DATABASE|API|RELEASE|DECISION") String documentType, Long categoryId, @NotBlank String content, @Size(max = 1000) String tags, @Pattern(regexp = "DRAFT|PUBLISHED") String status) {}
    public record TaskSave(@NotBlank @Size(max = 200) String title, String description, @Pattern(regexp = "TODO|IN_PROGRESS|DONE|CANCELLED") String status, @Pattern(regexp = "LOW|MEDIUM|HIGH|URGENT") String priority, @Min(0) @Max(100) Integer progress, LocalDate planDate, LocalDateTime dueTime, @Size(max = 200) String recurrenceRule) {}
    public record TaskStatusSave(@NotBlank @Pattern(regexp = "TODO|IN_PROGRESS|DONE|CANCELLED") String status, @Min(0) @Max(100) Integer progress) {}
}
