\set ON_ERROR_STOP on
SET client_encoding = 'UTF8';

-- 全局知识库问答使用 bge-m3 的 1024 维向量。关键词检索继续保留，向量不可用时自动降级。
CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE "kb_article_chunk"
  ADD COLUMN IF NOT EXISTS "embedding" vector(1024) NULL;

CREATE INDEX IF NOT EXISTS "kb_article_chunk_embedding_hnsw"
  ON "kb_article_chunk" USING hnsw ("embedding" vector_cosine_ops)
  WHERE "embedding" IS NOT NULL;

COMMENT ON COLUMN "kb_article_chunk"."embedding"
  IS '原始文档切片的内容向量，默认由 bge-m3 生成，维度 1024';
