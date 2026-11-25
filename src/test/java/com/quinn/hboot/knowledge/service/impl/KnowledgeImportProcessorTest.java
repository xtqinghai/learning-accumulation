package com.quinn.hboot.knowledge.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeImportProcessorTest {

    @Test
    void limitsSummaryAtSentenceBoundary() {
        String text = "这是一段用于测试摘要长度的文字。".repeat(100);

        String limited = KnowledgeImportProcessor.limitSummary(text, 300);

        assertTrue(limited.length() <= 300);
        assertTrue(limited.endsWith("。"));
    }

    @Test
    void hardLimitsSummaryWithoutSentenceBoundary() {
        String limited = KnowledgeImportProcessor.limitSummary("字".repeat(500), 300);

        assertEquals(300, limited.length());
    }

    @Test
    void formatsChapterWithOneHeadingAndSubordinateModelHeadings() {
        String formatted = KnowledgeImportProcessor.formatChapterSection(
            "第一章 本地知识库",
            "# 本地知识库：概述\n\n正文。\n\n## 操作步骤\n步骤内容。"
        );

        assertEquals("""
            ## 第一章 本地知识库

            正文。

            #### 操作步骤
            步骤内容。""", formatted);
    }

    @Test
    void capsNestedModelHeadingsAtSixLevels() {
        String formatted = KnowledgeImportProcessor.formatChapterSection(
            "第一章",
            "正文。\n\n###### 很深的标题\n内容。"
        );

        assertTrue(formatted.contains("\n\n###### 很深的标题\n内容。"));
        assertTrue(!formatted.contains("#######"));
    }
}
