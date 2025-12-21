/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80025 (8.0.25)
 Source Host           : 127.0.0.1:3306
 Source Schema         : h_boot

 Target Server Type    : MySQL
 Target Server Version : 80025 (8.0.25)
 File Encoding         : 65001

 Date: 21/12/2025 22:30:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gen_config
-- ----------------------------
DROP TABLE IF EXISTS `gen_config`;
CREATE TABLE `gen_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `table_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '表名',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块名',
  `package_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '包名',
  `business_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务名',
  `entity_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实体类名',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作者',
  `parent_menu_id` bigint NULL DEFAULT NULL COMMENT '上级菜单ID，对应sys_menu的id ',
  `remove_table_prefix` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '要移除的表前缀，如: sys_',
  `page_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '页面类型(classic|curd)',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tablename`(`table_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成基础配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_config
-- ----------------------------
INSERT INTO `gen_config` VALUES (11, 'sys_dept', 'system', 'com.quinn.hboot', '系统部门', 'Dept', 'yu-qh', NULL, 'sys_', 'classic', NULL, NULL, 0);
INSERT INTO `gen_config` VALUES (13, 'sys_login_log', 'system', 'com.quinn.hboot', '系统登录日志', 'LoginLog', 'yu-qh', 2, 'sys_', 'classic', NULL, NULL, 0);
INSERT INTO `gen_config` VALUES (16, 'sys_oper_log', 'system', 'com.quinn.hboot', '系统操作日志', 'OperLog', 'yu-qh', 2, 'sys_', 'classic', NULL, NULL, 0);

-- ----------------------------
-- Table structure for gen_field_config
-- ----------------------------
DROP TABLE IF EXISTS `gen_field_config`;
CREATE TABLE `gen_field_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_id` bigint NOT NULL COMMENT '关联的配置ID',
  `column_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `column_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `column_length` int NULL DEFAULT NULL,
  `field_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段名称',
  `field_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段类型',
  `field_sort` int NULL DEFAULT NULL COMMENT '字段排序',
  `field_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段描述',
  `max_length` int NULL DEFAULT NULL,
  `is_required` tinyint(1) NULL DEFAULT NULL COMMENT '是否必填',
  `is_show_in_list` tinyint(1) NULL DEFAULT 0 COMMENT '是否在列表显示',
  `is_show_in_form` tinyint(1) NULL DEFAULT 0 COMMENT '是否在表单显示',
  `is_show_in_query` tinyint(1) NULL DEFAULT 0 COMMENT '是否在查询条件显示',
  `query_type` tinyint NULL DEFAULT NULL COMMENT '查询方式',
  `form_type` tinyint NULL DEFAULT NULL COMMENT '表单类型',
  `dict_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典类型',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `config_id`(`config_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 176 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成字段配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of gen_field_config
-- ----------------------------
INSERT INTO `gen_field_config` VALUES (140, 11, 'id', 'bigint', NULL, 'id', 'Long', 1, '部门ID', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (141, 11, 'name', 'varchar', NULL, 'name', 'String', 2, '部门名称', 255, 1, 0, 1, 1, 1, 2, 'sex', NULL, NULL);
INSERT INTO `gen_field_config` VALUES (142, 11, 'parent_id', 'bigint', NULL, 'parentId', 'Long', 3, '父部门ID', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (143, 11, 'sort', 'int', NULL, 'sort', 'Integer', 4, '排序', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (144, 11, 'status', 'int', NULL, 'status', 'Integer', 5, '状态', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (145, 11, 'create_time', 'datetime', NULL, 'createTime', 'LocalDateTime', 6, '创建时间', NULL, 1, 0, 0, 0, 1, 9, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (146, 11, 'update_time', 'datetime', NULL, 'updateTime', 'LocalDateTime', 7, '更新时间', NULL, 1, 0, 0, 0, 1, 9, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (147, 11, 'id', 'bigint', NULL, 'id', 'Long', 1, '部门ID', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (148, 11, 'name', 'varchar', NULL, 'name', 'String', 2, '部门名称', 255, 1, 0, 1, 1, 1, 2, 'sex', NULL, NULL);
INSERT INTO `gen_field_config` VALUES (149, 11, 'parent_id', 'bigint', NULL, 'parentId', 'Long', 3, '父部门ID', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (150, 11, 'sort', 'int', NULL, 'sort', 'Integer', 4, '排序', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (151, 11, 'status', 'int', NULL, 'status', 'Integer', 5, '状态', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (152, 11, 'create_time', 'datetime', NULL, 'createTime', 'LocalDateTime', 6, '创建时间', NULL, 1, 0, 0, 0, 1, 9, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (153, 11, 'update_time', 'datetime', NULL, 'updateTime', 'LocalDateTime', 7, '更新时间', NULL, 1, 0, 0, 0, 1, 9, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (154, 13, 'id', 'bigint', NULL, 'id', 'Long', 1, '', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (155, 13, 'username', 'varchar', NULL, 'username', 'String', 2, '用户名', 50, 1, 1, 1, 1, 2, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (156, 13, 'ip', 'varchar', NULL, 'ip', 'String', 3, '登录IP', 50, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (157, 13, 'location', 'varchar', NULL, 'location', 'String', 4, 'IP归属地', 255, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (158, 13, 'browser', 'varchar', NULL, 'browser', 'String', 5, '浏览器', 100, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (159, 13, 'os', 'varchar', NULL, 'os', 'String', 6, '操作系统', 100, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (160, 13, 'status', 'tinyint', NULL, 'status', 'Integer', 7, '0失败 1成功', NULL, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (161, 13, 'msg', 'varchar', NULL, 'msg', 'String', 8, '提示信息', 255, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (162, 13, 'login_time', 'datetime', NULL, 'loginTime', 'LocalDateTime', 9, '登录时间', NULL, 0, 1, 1, 0, 1, 9, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (163, 16, 'id', 'bigint', NULL, 'id', 'Long', 1, '', NULL, 1, 0, 0, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (164, 16, 'module', 'varchar', NULL, 'module', 'String', 2, '模块名称', 50, 0, 1, 1, 1, 2, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (165, 16, 'type', 'varchar', NULL, 'type', 'String', 3, '操作类型', 20, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (166, 16, 'method', 'varchar', NULL, 'method', 'String', 4, '方法名', 255, 0, 1, 1, 1, 2, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (167, 16, 'request_method', 'varchar', NULL, 'requestMethod', 'String', 5, '请求方式', 10, 0, 1, 1, 1, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (168, 16, 'oper_name', 'varchar', NULL, 'operName', 'String', 6, '操作人', 50, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (169, 16, 'oper_ip', 'varchar', NULL, 'operIp', 'String', 7, '操作IP', 50, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (170, 16, 'oper_url', 'varchar', NULL, 'operUrl', 'String', 8, '请求URL', 255, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (171, 16, 'oper_param', 'text', NULL, 'operParam', 'String', 9, '请求参数', 65535, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (172, 16, 'json_result', 'text', NULL, 'jsonResult', 'String', 10, '返回结果', 65535, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (173, 16, 'status', 'tinyint', NULL, 'status', 'Integer', 11, '0失败 1成功', NULL, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (174, 16, 'error_msg', 'text', NULL, 'errorMsg', 'String', 12, '错误信息', 65535, 0, 1, 1, 0, 1, 1, NULL, NULL, NULL);
INSERT INTO `gen_field_config` VALUES (175, 16, 'oper_time', 'datetime', NULL, 'operTime', 'LocalDateTime', 13, '操作时间', NULL, 0, 1, 1, 0, 1, 9, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `parent_id` bigint NOT NULL COMMENT '父部门ID',
  `sort` int NOT NULL COMMENT '排序',
  `status` int NOT NULL COMMENT '状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统部门表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (1, '公司', 0, 1, 1, '2025-12-12 11:21:35', '2025-12-12 11:21:37');

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典类型',
  `dict_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态 1有效 0无效',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (6, 'sex', '性别', 1, '2025-12-15 14:36:48', '2025-12-15 14:36:47');

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `dict_id` bigint NOT NULL COMMENT '字典ID',
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '值',
  `sort` int NOT NULL COMMENT '排序',
  `status` int NOT NULL COMMENT '状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统字典项表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES (12, 6, '男', '1', 1, 1, '2025-12-15 14:37:11', '2025-12-15 14:36:56');
INSERT INTO `sys_dict_item` VALUES (13, 6, '女', '0', 2, 1, '2025-12-15 14:37:15', '2025-12-15 14:37:05');

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '登录IP',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP归属地',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` tinyint NULL DEFAULT NULL COMMENT '0失败 1成功',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提示信息',
  `login_time` datetime NULL DEFAULT NULL COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统登录日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `component` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单文件路径',
  `type` tinyint NOT NULL COMMENT '0目录 1菜单 2按钮',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '按钮权限标识',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '首页', '/dashboard', 'dashboard', 1, NULL, 'HomeFilled', 1, 1, '2025-12-12 11:24:36', '2025-12-20 10:12:13');
INSERT INTO `sys_menu` VALUES (2, 0, '系统管理', '/system', NULL, 0, NULL, 'Management', 2, 1, '2025-12-12 11:24:36', '2025-12-20 10:12:13');
INSERT INTO `sys_menu` VALUES (3, 2, '用户管理', '/system/user', 'system/user/userIndex', 1, NULL, 'UserFilled', 1, 1, '2025-12-12 11:24:36', '2025-12-12 17:23:14');
INSERT INTO `sys_menu` VALUES (4, 2, '角色管理', '/system/role', 'system/role/roleIndex', 1, NULL, 'User', 2, 1, '2025-12-12 11:24:36', '2025-12-20 10:12:13');
INSERT INTO `sys_menu` VALUES (5, 2, '菜单管理', '/system/menu', 'system/menu/menuIndex', 1, NULL, 'Menu', 3, 1, '2025-12-12 11:24:36', '2025-12-20 10:12:13');
INSERT INTO `sys_menu` VALUES (6, 2, '部门管理', '/system/dept', 'system/dept/deptIndex', 1, NULL, 'Discount', 4, 1, '2025-12-12 11:24:36', '2025-12-20 10:12:13');
INSERT INTO `sys_menu` VALUES (7, 2, '字典管理', '/system/dict', 'system/dict/dictIndex', 1, NULL, 'Notebook', 5, 1, '2025-12-12 11:24:36', '2025-12-20 10:12:13');
INSERT INTO `sys_menu` VALUES (8, 0, '代码生成', '/codegen', 'codegen/index', 1, NULL, '', 3, 1, '2025-12-15 14:49:33', '2025-12-15 14:53:54');
INSERT INTO `sys_menu` VALUES (9, 2, '登录日志', '/system/loginLog', 'system/login-log/index', 1, NULL, '', 9, 1, '2025-12-21 20:14:07', '2025-12-21 20:14:07');
INSERT INTO `sys_menu` VALUES (10, 2, '操作日志', '/system/operLog', 'system/oper-log/index', 1, NULL, '', 10, 1, '2025-12-21 20:14:07', '2025-12-21 20:14:07');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模块名称',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作类型',
  `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '方法名',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方式',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作人',
  `oper_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作IP',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求URL',
  `oper_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `json_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回结果',
  `status` tinyint NULL DEFAULT NULL COMMENT '0失败 1成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `oper_time` datetime NULL DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编码',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sort` int NULL DEFAULT NULL COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '系统管理员', 'system-admin', 1, NULL, '2025-12-12 11:22:28', '2025-12-12 11:22:30', NULL);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统角色菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (1, 2);
INSERT INTO `sys_role_menu` VALUES (1, 3);
INSERT INTO `sys_role_menu` VALUES (1, 4);
INSERT INTO `sys_role_menu` VALUES (1, 5);
INSERT INTO `sys_role_menu` VALUES (1, 6);
INSERT INTO `sys_role_menu` VALUES (1, 7);
INSERT INTO `sys_role_menu` VALUES (1, 8);
INSERT INTO `sys_role_menu` VALUES (1, 9);
INSERT INTO `sys_role_menu` VALUES (1, 10);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名称',
  `password` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '所属机构',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$7ARi0T918cXK4JAU0Y/IJuWuqilxcwnoJuFr.IKVLblCe5yV9JgK6', '管理员', 1, 1, '2025-12-11 11:28:23', '2025-12-11 15:16:26');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);

SET FOREIGN_KEY_CHECKS = 1;
