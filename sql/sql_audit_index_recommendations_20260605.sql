-- SQL audit and index recommendations
-- Review these statements against your production workload before execution.

CREATE INDEX idx_sys_user_dept_status ON sys_user (dept_id, status);
CREATE INDEX idx_sys_menu_parent_sort_status ON sys_menu (parent_id, sort, status);
CREATE INDEX idx_sys_role_menu_menu_id_role_id ON sys_role_menu (menu_id, role_id);
CREATE INDEX idx_sys_user_role_role_id_user_id ON sys_user_role (role_id, user_id);
CREATE INDEX idx_sys_file_status_create_time ON sys_file (status, create_time);
CREATE INDEX idx_flow_instance_business_id ON flow_instance (business_id);
CREATE INDEX idx_flow_instance_create_by_status ON flow_instance (create_by, flow_status);
