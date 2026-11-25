package com.quinn.hboot.knowledge.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Builds deterministic summary units before invoking the language model.
 *
 * <p>Documents with recognizable chapter headings are summarized chapter by chapter, using only
 * the configured number of leading characters from each chapter. Documents without chapters only
 * process the first 10,000 characters in ten 1,000-character units, which keeps local model
 * workloads bounded.</p>
 */
public final class KnowledgeSummarySegmenter {
    public static final int FALLBACK_CHUNK_CHARS = 1_000;
    public static final int FALLBACK_MAX_CHUNKS = 10;
    public static final int DEFAULT_CHAPTER_SUMMARY_CHARS = 1_000;

    private static final int MIN_CHAPTER_HEADINGS = 2;
    private static final int MAX_HEADING_LENGTH = 120;
    private static final int MAX_CHINESE_CHAPTER_HEADING_LENGTH = 60;
    private static final Pattern MARKDOWN_HEADING = Pattern.compile("^#{1,3}\\s+\\S.*$");
    private static final Pattern CHINESE_CHAPTER_HEADING = Pattern.compile(
        "^第\\s*[0-9０-９一二三四五六七八九十百千万零〇两]+\\s*[章卷篇部][^，。！？；]*$"
    );
    private static final Pattern ENGLISH_CHAPTER_HEADING = Pattern.compile(
        "^(?i:chapter|part)\\s+[0-9ivxlcdm]+(?:\\s*[:：.、-]?\\s+.*)?$"
    );
    private static final Pattern HEADING_NOISE = Pattern.compile("[\\s\\p{P}\\p{S}]+");

    public SummaryPlan plan(String text) {
        return plan(text, DEFAULT_CHAPTER_SUMMARY_CHARS);
    }

    public SummaryPlan plan(String text, int chapterSummaryChars) {
        if (text == null || text.isBlank()) {
            return new SummaryPlan(false, List.of());
        }
        List<Heading> headings = usableHeadings(findHeadings(text));
        if (headings.size() >= MIN_CHAPTER_HEADINGS) {
            List<Segment> chapterSegments = splitChapters(text, headings, Math.max(1, chapterSummaryChars));
            if (!chapterSegments.isEmpty()) {
                return new SummaryPlan(true, chapterSegments);
            }
        }
        return new SummaryPlan(false, splitFallback(text));
    }

    private List<Heading> findHeadings(String text) {
        List<Heading> headings = new ArrayList<>();
        boolean inFence = false;
        int lineStart = 0;
        while (lineStart <= text.length()) {
            int lineEnd = text.indexOf('\n', lineStart);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            String line = text.substring(lineStart, lineEnd);
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length() - 1);
            }
            String trimmed = line.strip();
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                inFence = !inFence;
            } else if (!inFence) {
                int markdownLevel = markdownLevel(trimmed);
                if (markdownLevel > 0 || isChineseOrEnglishHeading(trimmed)) {
                    int contentOffset = lineStart + Math.max(0, line.indexOf(trimmed));
                    headings.add(new Heading(contentOffset, trimmed, markdownLevel));
                }
            }
            if (lineEnd == text.length()) {
                break;
            }
            lineStart = lineEnd + 1;
        }
        return headings;
    }

    private int markdownLevel(String line) {
        if (line.isBlank() || line.length() > MAX_HEADING_LENGTH) {
            return 0;
        }
        int level = 0;
        while (level < line.length() && level < 3 && line.charAt(level) == '#') {
            level++;
        }
        if (level == 0 || level >= line.length() || !Character.isWhitespace(line.charAt(level))) {
            return 0;
        }
        return MARKDOWN_HEADING.matcher(line).matches() ? level : 0;
    }

    private boolean isChineseOrEnglishHeading(String line) {
        if (line.isBlank() || line.length() > MAX_HEADING_LENGTH) {
            return false;
        }
        return ENGLISH_CHAPTER_HEADING.matcher(line).matches()
            || (line.length() <= MAX_CHINESE_CHAPTER_HEADING_LENGTH
                && CHINESE_CHAPTER_HEADING.matcher(line).matches());
    }

    /**
     * A table of contents repeats the same headings immediately before the body. Keep the last
     * occurrence so the navigation page does not become a series of near-empty chapters.
     */
    private List<Heading> usableHeadings(List<Heading> headings) {
        List<Heading> relevant = selectChapterLevel(headings);
        if (relevant.size() < 2) {
            return relevant;
        }
        Map<String, Integer> lastIndexByTitle = new LinkedHashMap<>();
        for (int index = 0; index < relevant.size(); index++) {
            lastIndexByTitle.put(headingKey(relevant.get(index).title()), index);
        }
        List<Heading> result = new ArrayList<>();
        for (int index = 0; index < relevant.size(); index++) {
            if (lastIndexByTitle.get(headingKey(relevant.get(index).title())) == index) {
                result.add(relevant.get(index));
            }
        }
        return result;
    }

    private List<Heading> selectChapterLevel(List<Heading> headings) {
        List<Heading> textChapters = headings.stream().filter(heading -> heading.markdownLevel() == 0).toList();
        if (textChapters.size() >= MIN_CHAPTER_HEADINGS) {
            return textChapters;
        }
        for (int level = 1; level <= 3; level++) {
            int selectedLevel = level;
            List<Heading> markdownChapters = headings.stream()
                .filter(heading -> heading.markdownLevel() == selectedLevel)
                .toList();
            if (markdownChapters.size() >= MIN_CHAPTER_HEADINGS) {
                return markdownChapters;
            }
        }
        return List.of();
    }

    private String headingKey(String title) {
        return HEADING_NOISE.matcher(title).replaceAll("").toLowerCase(Locale.ROOT);
    }

    private List<Segment> splitChapters(String text, List<Heading> headings, int chapterSummaryChars) {
        List<Segment> segments = new ArrayList<>();
        for (int index = 0; index < headings.size(); index++) {
            Heading heading = headings.get(index);
            int end = index + 1 < headings.size() ? headings.get(index + 1).offset() : text.length();
            if (end <= heading.offset()) {
                continue;
            }
            String chapterText = text.substring(heading.offset(), end).strip();
            if (chapterText.isBlank()) {
                continue;
            }
            String limited = chapterText.substring(0, Math.min(chapterText.length(), chapterSummaryChars));
            segments.add(new Segment(segments.size(), displayTitle(heading.title()), limited));
        }
        return segments;
    }

    private String displayTitle(String title) {
        return title.replaceFirst("^#{1,3}\\s+", "").strip();
    }

    private List<Segment> splitFallback(String text) {
        String limited = text.substring(0, Math.min(text.length(), FALLBACK_CHUNK_CHARS * FALLBACK_MAX_CHUNKS));
        List<String> parts = splitByChars(limited, FALLBACK_CHUNK_CHARS);
        List<Segment> segments = new ArrayList<>(parts.size());
        for (int index = 0; index < parts.size(); index++) {
            segments.add(new Segment(index, "第 " + (index + 1) + "/" + parts.size() + " 段", parts.get(index)));
        }
        return segments;
    }

    private List<String> splitByChars(String value, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < value.length()) {
            int end = Math.min(start + chunkSize, value.length());
            chunks.add(value.substring(start, end));
            start = end;
        }
        return chunks;
    }

    private record Heading(int offset, String title, int markdownLevel) {
    }

    public record Segment(int number, String label, String text) {
    }

    public record SummaryPlan(boolean chapterBased, List<Segment> segments) {
    }
}
