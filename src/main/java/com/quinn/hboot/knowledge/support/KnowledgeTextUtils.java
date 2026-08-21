package com.quinn.hboot.knowledge.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class KnowledgeTextUtils {
    private static final int MAX_SEARCH_TOKENS = 30;

    private KnowledgeTextUtils() {
    }

    public static List<String> buildSearchTokens(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String normalized = keyword.trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[\\p{P}\\p{S}\\s]+", "");
        if (normalized.isEmpty()) {
            return List.of();
        }
        int[] codePoints = normalized.codePoints().toArray();
        if (codePoints.length == 1) {
            return List.of(normalized);
        }
        List<String> tokens = new ArrayList<>();
        for (int index = 0; index < codePoints.length - 1 && tokens.size() < MAX_SEARCH_TOKENS; index++) {
            String token = new String(codePoints, index, 2);
            if (!tokens.contains(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public static String toSearchText(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        return markdown
            .replaceAll("(?s)```[a-zA-Z0-9_-]*\\s*", " ")
            .replace("```", " ")
            .replaceAll("!\\[([^]]*)]\\([^)]*\\)", "$1")
            .replaceAll("\\[([^]]+)]\\([^)]*\\)", "$1")
            .replaceAll("<[^>]+>", " ")
            .replaceAll("(?m)^#{1,6}\\s*", "")
            .replaceAll("[*_~>`|]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}
