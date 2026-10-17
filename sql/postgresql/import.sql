\set ON_ERROR_STOP on
\echo 'Importing h_boot PostgreSQL schema and data...'
BEGIN;
\ir 00_h_boot_schema_data.sql
\ir 05_sync_base_sequences.sql
\ir 10_knowledge_base_v1.sql
\ir 20_photo_album_v1.sql
\ir 30_sa_token_permission_patch_20260604.sql
\ir 40_update_warm_flow_1_8_5.sql
\ir 50_sql_audit_indexes.sql
\ir 99_sync_sequences.sql
COMMIT;
\echo 'h_boot import completed.'
