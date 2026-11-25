package com.quinn.hboot.knowledge.support;

import com.quinn.hboot.knowledge.support.KnowledgeSummarySegmenter.Segment;
import com.quinn.hboot.knowledge.support.KnowledgeSummarySegmenter.SummaryPlan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeSummarySegmenterTest {

    private final KnowledgeSummarySegmenter segmenter = new KnowledgeSummarySegmenter();

    @Test
    void usesChapterHeadingsAndSkipsTableOfContentsDuplicates() {
        String text = """
            目录
            第1章 开始
            第2章 过程

            第1章 开始
            第一章正文内容。

            第2章 过程
            第二章正文内容。
            """;

        SummaryPlan plan = segmenter.plan(text);

        assertTrue(plan.chapterBased());
        assertEquals(2, plan.segments().size());
        assertEquals("第1章 开始", plan.segments().get(0).label());
        assertTrue(plan.segments().get(0).text().contains("第一章正文内容"));
        assertFalse(plan.segments().get(0).text().contains("目录"));
        assertEquals("第2章 过程", plan.segments().get(1).label());
    }

    @Test
    void fallsBackToFirstTenThousandCharactersInTenUnits() {
        String text = "字".repeat(25_000);

        SummaryPlan plan = segmenter.plan(text);

        assertFalse(plan.chapterBased());
        assertEquals(10, plan.segments().size());
        assertEquals(1_000, plan.segments().get(0).text().length());
        assertEquals(10_000, plan.segments().stream().mapToInt(segment -> segment.text().length()).sum());
    }

    @Test
    void limitsEachChapterToTheFirstOneThousandCharactersByDefault() {
        String text = "第1章 开始\n" + "甲".repeat(13_000) + "\n第2章 过程\n乙".repeat(100);

        SummaryPlan plan = segmenter.plan(text);

        assertTrue(plan.chapterBased());
        assertEquals(2, plan.segments().size());
        assertEquals("第1章 开始", plan.segments().get(0).label());
        assertEquals(1_000, plan.segments().get(0).text().length());
        assertEquals("第2章 过程", plan.segments().get(1).label());
        assertTrue(plan.segments().get(1).text().length() <= 1_000);
    }

    @Test
    void supportsAConfiguredChapterCharacterLimit() {
        String text = "第1章 开始\n" + "甲".repeat(3_000) + "\n第2章 过程\n" + "乙".repeat(3_000);

        SummaryPlan plan = segmenter.plan(text, 1_200);

        assertTrue(plan.chapterBased());
        assertEquals(2, plan.segments().size());
        assertEquals(1_200, plan.segments().get(0).text().length());
        assertEquals(1_200, plan.segments().get(1).text().length());
    }

    @Test
    void doesNotTreatNarrativeSentencesAsChapterHeadings() {
        String text = """
            第3章提到，Pod中托管了一个或多个容器。从规格的角度来看，
            第12章展开讨论。不过在此之前，我们有必要了解安全威胁，
            第三回合汰弱留强，公司交由工程师主导。
            """;

        SummaryPlan plan = segmenter.plan(text);

        assertFalse(plan.chapterBased());
        assertEquals(1, plan.segments().size());
    }

    @Test
    void choosesTheHighestMarkdownLevelWithMultipleSections() {
        String text = """
            # 文档标题
            简介

            ## 第一节
            第一节正文

            ### 子项
            子项正文

            ## 第二节
            第二节正文
            """;

        SummaryPlan plan = segmenter.plan(text);

        assertTrue(plan.chapterBased());
        assertEquals(2, plan.segments().size());
        assertEquals("第一节", plan.segments().get(0).label());
        assertEquals("第二节", plan.segments().get(1).label());
    }

    @Test
    void ignoresMarkdownHeadingsInsideCodeFences() {
        String text = """
            # 第一章
            正文一

            ```md
            # 代码里的标题
            ```

            # 第二章
            正文二
            """;

        SummaryPlan plan = segmenter.plan(text);

        assertTrue(plan.chapterBased());
        assertEquals(2, plan.segments().size());
        assertEquals("第一章", plan.segments().get(0).label());
        assertTrue(plan.segments().get(0).text().contains("# 代码里的标题"));
        assertEquals("第二章", plan.segments().get(1).label());
    }
}
