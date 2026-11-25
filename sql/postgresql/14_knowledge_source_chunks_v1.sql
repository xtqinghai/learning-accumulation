\set ON_ERROR_STOP on
SET client_encoding = 'UTF8';

-- RAG 检索切片应来自原始文档，而不是 AI 生成的文章摘要。
ALTER TABLE "kb_article_chunk"
  ADD COLUMN IF NOT EXISTS "source_file_id" bigint NULL;

CREATE INDEX IF NOT EXISTS "kb_article_chunk_source_file"
  ON "kb_article_chunk" ("space_id", "source_file_id", "chunk_no");

COMMENT ON COLUMN "kb_article_chunk"."source_file_id"
  IS '原始文档 ID；为空表示文章是手工创作或没有可用的原文';

-- 历史导入文章原先保存的是摘要切片，统一按 parsed_text 重建为原文切片。
DELETE FROM "kb_article_chunk" c
USING "kb_article" a, "kb_source_file" s
WHERE c."article_id" = a."id"
  AND a."source_file_id" = s."id"
  AND a."deleted" = 0
  AND NULLIF(BTRIM(s."parsed_text"), '') IS NOT NULL;

WITH imported AS (
  SELECT
    a."id" AS article_id,
    a."space_id" AS space_id,
    s."id" AS source_file_id,
    BTRIM(s."parsed_text") AS parsed_text
  FROM "kb_article" a
  JOIN "kb_source_file" s ON s."id" = a."source_file_id"
  WHERE a."deleted" = 0
    AND NULLIF(BTRIM(s."parsed_text"), '') IS NOT NULL
),
generated AS (
  SELECT
    imported.article_id,
    imported.space_id,
    imported.source_file_id,
    start_pos,
    SUBSTRING(imported.parsed_text FROM start_pos + 1 FOR 1200) AS content
  FROM imported
  CROSS JOIN LATERAL GENERATE_SERIES(
    0,
    GREATEST(CHAR_LENGTH(imported.parsed_text) - 1, 0),
    1050
  ) AS start_pos
)
INSERT INTO "kb_article_chunk" (
  "article_id", "source_file_id", "space_id", "chunk_no", "content",
  "search_content", "char_start", "char_end", "create_time", "update_time"
)
SELECT
  article_id,
  source_file_id,
  space_id,
  ROW_NUMBER() OVER (PARTITION BY article_id ORDER BY start_pos) - 1,
  content,
  content,
  start_pos,
  start_pos + CHAR_LENGTH(content),
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
FROM generated
WHERE NULLIF(BTRIM(content), '') IS NOT NULL;
