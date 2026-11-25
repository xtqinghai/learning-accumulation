\set ON_ERROR_STOP on
\echo 'Importing h_boot PostgreSQL schema and data...'
BEGIN;
\ir 00_h_boot_schema_data.sql
\ir 05_sync_base_sequences.sql
\ir 10_knowledge_base_v1.sql
\ir 11_knowledge_productivity_v2.sql
\ir 12_knowledge_rag_v1.sql
\ir 20_photo_album_v1.sql
\ir 21_family_assets_v2.sql
\ir 30_sa_token_permission_patch_20260604.sql
\ir 40_update_warm_flow_1_8_5.sql
\ir 50_sql_audit_indexes.sql
\ir 60_notification_v1.sql
\ir 70_scheduled_job_v1.sql
\ir 80_parameter_backup_v1.sql
\ir 13_knowledge_summary_config_v1.sql
\ir 14_knowledge_source_chunks_v1.sql
\ir 15_knowledge_summary_runtime_v1.sql
\ir 16_knowledge_embeddings_v1.sql
\ir 92_tres_demo_menu.sql
\ir 99_sync_sequences.sql
COMMIT;
\echo 'h_boot import completed.'
