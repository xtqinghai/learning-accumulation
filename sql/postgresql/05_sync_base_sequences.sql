\set ON_ERROR_STOP on
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;

SET escape_string_warning = off;

-- Advance identity sequences past explicitly imported IDs.
DO $sync_sequences$
DECLARE
  next_value bigint;
BEGIN
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "gen_config";
  PERFORM setval(pg_get_serial_sequence('gen_config', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "gen_field_config";
  PERFORM setval(pg_get_serial_sequence('gen_field_config', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "ps_album";
  PERFORM setval(pg_get_serial_sequence('ps_album', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "ps_album_file";
  PERFORM setval(pg_get_serial_sequence('ps_album_file', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "ps_file";
  PERFORM setval(pg_get_serial_sequence('ps_file', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "ps_file_tag";
  PERFORM setval(pg_get_serial_sequence('ps_file_tag', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "ps_tag";
  PERFORM setval(pg_get_serial_sequence('ps_tag', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "ps_timeline";
  PERFORM setval(pg_get_serial_sequence('ps_timeline', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_cms_content";
  PERFORM setval(pg_get_serial_sequence('sys_cms_content', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_dept";
  PERFORM setval(pg_get_serial_sequence('sys_dept', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_dict";
  PERFORM setval(pg_get_serial_sequence('sys_dict', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_dict_item";
  PERFORM setval(pg_get_serial_sequence('sys_dict_item', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_file";
  PERFORM setval(pg_get_serial_sequence('sys_file', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_login_log";
  PERFORM setval(pg_get_serial_sequence('sys_login_log', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_menu";
  PERFORM setval(pg_get_serial_sequence('sys_menu', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_oper_log";
  PERFORM setval(pg_get_serial_sequence('sys_oper_log', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_role";
  PERFORM setval(pg_get_serial_sequence('sys_role', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_user";
  PERFORM setval(pg_get_serial_sequence('sys_user', 'id'), next_value, false);
  SELECT COALESCE(MAX("id"), 0) + 1 INTO next_value FROM "sys_user_org_post";
  PERFORM setval(pg_get_serial_sequence('sys_user_org_post', 'id'), next_value, false);
END
$sync_sequences$;
