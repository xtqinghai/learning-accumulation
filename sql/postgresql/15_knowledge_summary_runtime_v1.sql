\set ON_ERROR_STOP on
SET client_encoding = 'UTF8';

-- 单次总结请求的最大输出 token 数。通过 Spring AI ChatOptions 覆盖全局默认值。
INSERT INTO "sys_system_parameter"
  ("parameter_key", "parameter_name", "parameter_value", "category", "sensitive", "status", "remark", "creator", "create_time", "updater", "update_time")
SELECT
  'knowledge.summary.max-tokens',
  '知识库单次总结最大输出 Token',
  '1024',
  '知识库',
  0,
  1,
  '单次章节或分段总结允许生成的最大 token 数，允许范围 128-8192，默认 1024。',
  'system',
  CURRENT_TIMESTAMP,
  'system',
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM "sys_system_parameter" WHERE "parameter_key" = 'knowledge.summary.max-tokens'
);

-- 本地 2B 模型使用更长的调用间隔，降低持续满载和机身温度；用户已自定义的值不会被覆盖。
UPDATE "sys_system_parameter"
SET "parameter_value" = '3000',
    "remark" = '每次调用大模型之间的停顿毫秒数，0 表示不停顿，最大 60000；本地小模型散热建议 3000-5000。',
    "update_time" = CURRENT_TIMESTAMP
WHERE "parameter_key" = 'knowledge.summary.interval-ms'
  AND "parameter_value" = '1000';
