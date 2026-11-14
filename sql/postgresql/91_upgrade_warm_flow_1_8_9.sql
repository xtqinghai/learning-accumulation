-- Upgrade Warm-Flow schema from the 1.8.5 example to 1.8.9.
-- Only columns actually mapped by the 1.8.9 MyBatis-Plus entities are added.
-- Run before using the built-in form APIs.

ALTER TABLE flow_definition
  ADD COLUMN IF NOT EXISTS flow_name varchar(100);

-- New definitions stay editable until explicitly activated from the list.
ALTER TABLE flow_definition
  ALTER COLUMN activity_status SET DEFAULT 0;

-- Existing 1.8.5 databases did not persist the definition name. Recover it from
-- the latest instance snapshot, then fall back to the immutable flow code.
UPDATE flow_definition fd
SET flow_name = COALESCE(
  NULLIF(fd.flow_name, ''),
  (
    SELECT fi.def_json::jsonb ->> 'flowName'
    FROM flow_instance fi
    WHERE fi.definition_id = fd.id
      AND fi.def_json IS NOT NULL
    ORDER BY fi.create_time DESC
    LIMIT 1
  ),
  fd.flow_code
)
WHERE fd.flow_name IS NULL OR TRIM(fd.flow_name) = '';

ALTER TABLE flow_his_task
  ADD COLUMN IF NOT EXISTS create_by varchar(64) DEFAULT '',
  ADD COLUMN IF NOT EXISTS update_by varchar(64) DEFAULT '';

ALTER TABLE flow_form
  ADD COLUMN IF NOT EXISTS create_by varchar(64) DEFAULT '',
  ADD COLUMN IF NOT EXISTS update_by varchar(64) DEFAULT '';

CREATE INDEX IF NOT EXISTS flow_definition_code_publish_idx
  ON flow_definition (flow_code, is_publish);
CREATE INDEX IF NOT EXISTS flow_instance_business_idx
  ON flow_instance (business_id);
