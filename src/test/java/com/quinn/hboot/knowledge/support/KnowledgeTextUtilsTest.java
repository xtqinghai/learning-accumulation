package com.quinn.hboot.knowledge.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeTextUtilsTest {

    @Test
    void buildsOverlappingChineseBigramsForFuzzyRecall() {
        List<String> queryTokens = KnowledgeTextUtils.buildSearchTokens("我的高中老师");
        List<String> articleTokens = KnowledgeTextUtils.buildSearchTokens("我的老师");

        assertEquals(List.of("我的", "的高", "高中", "中老", "老师"), queryTokens);
        assertTrue(queryTokens.stream().anyMatch(articleTokens::contains));
        assertTrue(queryTokens.contains("老师"));
    }

    @Test
    void normalizesMarkdownToSearchablePlainText() {
        String markdown = "# 我的老师\n\n[王老师](https://example.com) **认真负责**。";

        assertEquals("我的老师 王老师 认真负责 。", KnowledgeTextUtils.toSearchText(markdown));
    }
}
