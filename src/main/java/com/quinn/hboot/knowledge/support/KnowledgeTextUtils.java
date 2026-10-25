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

    public record Chunk(String content, String searchContent, int chunkNo, int charStart, int charEnd) {
    }

    public static List<Chunk> splitIntoChunks(String content, int size, int overlap) {
        if (content == null || content.isBlank()) {
            return List.of();
        }
        int safeSize = Math.max(200, size);
        int safeOverlap = Math.min(Math.max(0, overlap), safeSize - 1);
        String normalized = content.strip();
        List<Chunk> chunks = new ArrayList<>();
        int codePointCount = normalized.codePointCount(0, normalized.length());
        int codePointStart = 0;
        int chunkNo = 0;
        while (codePointStart < codePointCount && chunkNo < 1000) {
            int charOffset = normalized.offsetByCodePoints(0, codePointStart);
            int length = Math.min(safeSize, codePointCount - codePointStart);
            int charEnd = normalized.offsetByCodePoints(charOffset, length);
            String chunkContent = normalized.substring(charOffset, charEnd);
            chunks.add(new Chunk(
                chunkContent,
                toSearchText(chunkContent),
                chunkNo++,
                charOffset,
                charEnd
            ));
            if (charEnd >= normalized.length()) {
                break;
            }
            codePointStart += safeSize - safeOverlap;
        }
        return chunks;
    }
}
