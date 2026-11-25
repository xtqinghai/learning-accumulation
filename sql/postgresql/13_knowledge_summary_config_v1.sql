\set ON_ERROR_STOP on
SET client_encoding = 'UTF8';

-- 知识库总结调用间隔：0 表示连续调用，最大允许 60000 毫秒。
INSERT INTO "sys_system_parameter"
  ("parameter_key", "parameter_name", "parameter_value", "category", "sensitive", "status", "remark", "creator", "create_time", "updater", "update_time")
SELECT
  'knowledge.summary.interval-ms',
  '知识库总结调用间隔',
  '1000',
  '知识库',
  0,
  1,
  '每次调用大模型进行分段或合并总结之间的停顿毫秒数，0 表示不停顿，最大 60000。',
  'system',
  CURRENT_TIMESTAMP,
  'system',
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM "sys_system_parameter" WHERE "parameter_key" = 'knowledge.summary.interval-ms'
);

-- 每章仅总结前多少个字符。章节过长时不会继续拆分，直接截取该前缀。
INSERT INTO "sys_system_parameter"
  ("parameter_key", "parameter_name", "parameter_value", "category", "sensitive", "status", "remark", "creator", "create_time", "updater", "update_time")
SELECT
  'knowledge.summary.chapter-chars',
  '知识库单章总结字符数',
  '1000',
  '知识库',
  0,
  1,
  '每个章节仅总结前多少个字符，允许范围 100-10000，默认 1000。',
  'system',
  CURRENT_TIMESTAMP,
  'system',
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM "sys_system_parameter" WHERE "parameter_key" = 'knowledge.summary.chapter-chars'
);

-- 每个章节或分段摘要的目标字数，也是无章节模式下最终合并摘要的字数上限。
INSERT INTO "sys_system_parameter"
  ("parameter_key", "parameter_name", "parameter_value", "category", "sensitive", "status", "remark", "creator", "create_time", "updater", "update_time")
SELECT
  'knowledge.summary.output-chars',
  '知识库分段摘要字数',
  '300',
  '知识库',
  0,
  1,
  '每个章节或分段摘要的目标字数，允许范围 50-2000，默认 300。',
  'system',
  CURRENT_TIMESTAMP,
  'system',
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM "sys_system_parameter" WHERE "parameter_key" = 'knowledge.summary.output-chars'
);
