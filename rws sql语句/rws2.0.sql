/*
Navicat MySQL Data Transfer

Source Server         : 10.0.5.55
Source Server Version : 50719
Source Host           : 10.0.5.55:3306
Source Database       : rws

Target Server Type    : MYSQL
Target Server Version : 50719
File Encoding         : 65001

Date: 2019-07-04 16:15:02
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for active_index
-- ----------------------------
DROP TABLE IF EXISTS `active_index`;
CREATE TABLE `active_index` (
  `id` varchar(64) NOT NULL,
  `confirm_active_id` varchar(64) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `project_id` varchar(64) NOT NULL,
  `project_name` varchar(100) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `create_user` varchar(50) DEFAULT NULL,
  `update_user` varchar(50) DEFAULT NULL,
  `mark` text,
  `active_type` tinyint(4) DEFAULT NULL COMMENT '类型：1：活动；2指标，3：入排条件',
  `is_tmp` tinyint(4) DEFAULT NULL COMMENT '检索标识，1是检索，0：是保存',
  `sort_key` varchar(200) DEFAULT NULL,
  `data_group` varchar(500) DEFAULT NULL,
  `is_variant` tinyint(4) DEFAULT '0' COMMENT '是否是研究变量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for active_index_config
-- ----------------------------
DROP TABLE IF EXISTS `active_index_config`;
CREATE TABLE `active_index_config` (
  `id` varchar(64) NOT NULL,
  `active_index_id` varchar(64) NOT NULL,
  `project_name` varchar(100) DEFAULT NULL,
  `active_result_desc` varchar(200) DEFAULT NULL,
  `active_result` varchar(100) DEFAULT NULL,
  `index_result_value` varchar(50) DEFAULT NULL,
  `index_type` varchar(20) DEFAULT NULL,
  `index_type_desc` varchar(200) DEFAULT NULL,
  `operator` varchar(20) DEFAULT NULL,
  `operator_num` varchar(20) DEFAULT NULL,
  `function` varchar(20) DEFAULT NULL,
  `function_param` varchar(20) DEFAULT NULL,
  `index_column` varchar(100) DEFAULT NULL,
  `index_column_desc` varchar(200) DEFAULT NULL,
  `search_scope` varchar(100) DEFAULT NULL,
  `mark` varchar(200) DEFAULT NULL,
  `is_not_used` tinyint(4) DEFAULT NULL,
  `date_format` varchar(20) DEFAULT NULL COMMENT '前端用来格式化数据',
  `index_result_value_is_equal` varchar(50) DEFAULT NULL,
  `is_other` int(40) DEFAULT NULL COMMENT '判断枚举其他类',
  PRIMARY KEY (`id`),
  KEY `index_active_index_id` (`active_index_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for active_index_config_condition
-- ----------------------------
DROP TABLE IF EXISTS `active_index_config_condition`;
CREATE TABLE `active_index_config_condition` (
  `id` varchar(64) NOT NULL,
  `active_index_config_id` varchar(64) DEFAULT NULL,
  `source_tag_name_desc` varchar(200) DEFAULT NULL COMMENT '原比较路径描述',
  `source_tag_name` varchar(100) DEFAULT NULL COMMENT '原比较路径',
  `target_tag_name_desc` varchar(200) DEFAULT NULL COMMENT '目标路径描述',
  `target_tag_name` varchar(100) DEFAULT NULL COMMENT '目标路径',
  `operator_sign` varchar(50) DEFAULT NULL COMMENT '操作符',
  `operator_sign_desc` varchar(50) DEFAULT NULL COMMENT '操作符描述',
  `ref_relation` varchar(100) DEFAULT NULL COMMENT '关联关系 direct, ref, leftref',
  `logic_sing` varchar(8) DEFAULT NULL COMMENT '逻辑运算符',
  `ref_active_id` varchar(64) DEFAULT NULL COMMENT '引用的id',
  `value` longtext COMMENT '值',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '父级id',
  `ref_active_name` varchar(100) DEFAULT NULL COMMENT '引用的活动名称',
  `type` tinyint(4) DEFAULT NULL COMMENT '1:组别;2:条件',
  `level` int(11) DEFAULT NULL,
  `need_path` varchar(255) DEFAULT NULL,
  `condition_type` tinyint(4) DEFAULT NULL COMMENT '条件类型，1：指标条件，2：事件条件，3：入组条件，4：排除条件',
  `uuid` varchar(70) DEFAULT NULL COMMENT 'webui前端回填数据使用',
  `node_type` varchar(20) DEFAULT NULL COMMENT '节点类型',
  `acceptance_state` tinyint(4) DEFAULT NULL COMMENT '是否接收 0：不接受，1：接收',
  `before` varchar(40) DEFAULT NULL,
  `after` varchar(40) DEFAULT NULL,
  `title_info` varchar(255) DEFAULT NULL COMMENT '头标识',
  `json_type` varchar(100) DEFAULT NULL,
  `inner_lever` int(40) DEFAULT NULL COMMENT '判断颜色层深',
  `orde` int(40) DEFAULT NULL COMMENT '排序字段',
  `title_type` varchar(100) DEFAULT NULL,
  `children_key` varchar(100) DEFAULT NULL COMMENT '检验子项key',
  `enum_active_config_id` longtext,
  PRIMARY KEY (`id`),
  KEY `active_index_config_id` (`active_index_config_id`) USING BTREE,
  KEY `index_parent_id` (`parent_id`,`type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for active_index_task
-- ----------------------------
DROP TABLE IF EXISTS `active_index_task`;
CREATE TABLE `active_index_task` (
  `id` varchar(64) NOT NULL,
  `active_index_id` varchar(64) NOT NULL,
  `project_id` varchar(64) NOT NULL,
  `submit_time` timestamp NULL DEFAULT NULL,
  `status` int(11) DEFAULT NULL COMMENT '0:任务已提交；1：任务执行成功，2：任务失败',
  `message` varchar(500) DEFAULT NULL,
  `complate_time` timestamp NULL DEFAULT NULL,
  `case_total` int(11) DEFAULT NULL,
  `market_apply` int(11) DEFAULT NULL,
  `search_result` int(11) DEFAULT NULL,
  `contain_apply` int(11) DEFAULT NULL,
  `submit_num` tinyint(4) DEFAULT NULL COMMENT '执行次数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_active_sql_map
-- ----------------------------
DROP TABLE IF EXISTS `p_active_sql_map`;
CREATE TABLE `p_active_sql_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` varchar(50) DEFAULT NULL COMMENT '项目id',
  `active_index_id` varchar(50) DEFAULT NULL COMMENT '指标id',
  `active_sql` longtext COMMENT 'sql语句',
  `index_result_value` varchar(255) DEFAULT NULL COMMENT '枚举值',
  `sql_select` longtext,
  `sql_from` longtext,
  `sql_where` longtext,
  `source_filtere` longtext,
  `active_type` varchar(5) DEFAULT NULL COMMENT '指标类型',
  `ref_active_ids` text COMMENT '引用指标ids',
  `source_value` text COMMENT 'sourceValue',
  `active_result_doc_id` varchar(255) DEFAULT NULL COMMENT '事件搜索条件',
  `active_result_value` varchar(100) DEFAULT NULL,
  `event_where` longtext COMMENT '那排事件where',
  `select_value` varchar(200) DEFAULT NULL COMMENT '搜索结果函数',
  `index_type_value` varchar(16) DEFAULT NULL COMMENT '指标数据类型',
  `active_other_result` longtext COMMENT ' 事件差集',
  `active_name` varchar(16) DEFAULT NULL,
  `count_value` varchar(100) DEFAULT NULL,
  `sqlMd5` varchar(100) DEFAULT NULL COMMENT 'sqlMd5',
  `is_other` int(2) DEFAULT '0' COMMENT '是否是其',
  `sql_having` varchar(255) DEFAULT '' COMMENT 'sql_having',
  `group_id` varchar(52) DEFAULT NULL COMMENT '分组id',
  PRIMARY KEY (`id`),
  KEY `active_index_id` (`active_index_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=22024 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_contrastive_analysis_active
-- ----------------------------
DROP TABLE IF EXISTS `p_contrastive_analysis_active`;
CREATE TABLE `p_contrastive_analysis_active` (
  `id` int(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `active_index_id` varchar(46) DEFAULT '' COMMENT '指标id',
  `project_id` varchar(46) DEFAULT NULL COMMENT '项目id',
  `create_id` varchar(46) DEFAULT NULL COMMENT '创建人id',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` varchar(46) DEFAULT NULL COMMENT '修改用户id',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `cort_type` int(4) DEFAULT NULL COMMENT '区分统计图形和患者列表',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9524 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_contrastive_analysis_count
-- ----------------------------
DROP TABLE IF EXISTS `p_contrastive_analysis_count`;
CREATE TABLE `p_contrastive_analysis_count` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(32) DEFAULT NULL COMMENT '用户id',
  `projectId` varchar(42) DEFAULT NULL COMMENT '项目id',
  `activer_index_id` varchar(42) DEFAULT NULL COMMENT '研究变量id',
  `group_type` varchar(100) DEFAULT NULL COMMENT '分组类型',
  `contrastive_analysis_count_result_id` varchar(42) DEFAULT NULL COMMENT '结果列表id',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建用户id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建用户名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `group_id` varchar(42) DEFAULT NULL,
  `group_name` varchar(255) DEFAULT NULL,
  `active_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_contrastive_analysis_count_result
-- ----------------------------
DROP TABLE IF EXISTS `p_contrastive_analysis_count_result`;
CREATE TABLE `p_contrastive_analysis_count_result` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `active_index_id` varchar(42) DEFAULT NULL COMMENT '研究变量id',
  `col_name` varchar(100) DEFAULT NULL COMMENT '列名',
  `col_value` varchar(100) DEFAULT NULL COMMENT '列值',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_contrastive_analysis_patient
-- ----------------------------
DROP TABLE IF EXISTS `p_contrastive_analysis_patient`;
CREATE TABLE `p_contrastive_analysis_patient` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(42) DEFAULT NULL COMMENT '用户id',
  `projectId` varchar(42) DEFAULT NULL COMMENT '项目id',
  `col_name` varchar(100) DEFAULT NULL COMMENT '列明',
  `col_value` varchar(100) DEFAULT NULL COMMENT '列值',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建用户id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建用户名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `row_num` int(11) DEFAULT NULL,
  `col_id` varchar(42) DEFAULT NULL COMMENT '研究变量id',
  `patient_sn` varchar(42) DEFAULT '' COMMENT '患者编号',
  `group_id` varchar(42) DEFAULT NULL COMMENT '分组id',
  `group_name` varchar(100) DEFAULT NULL COMMENT '所选组名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_group
-- ----------------------------
DROP TABLE IF EXISTS `p_group`;
CREATE TABLE `p_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'int',
  `group_id` varchar(42) NOT NULL COMMENT '分组id',
  `group_name` varchar(100) DEFAULT NULL COMMENT '分组名称',
  `group_parent_id` varchar(42) DEFAULT NULL COMMENT '上级组id',
  `group_describe` text COMMENT '分组描述',
  `group_level` int(11) DEFAULT '0' COMMENT '分组等级',
  `group_type_id` varchar(42) DEFAULT NULL COMMENT '分组类型',
  `project_id` varchar(42) DEFAULT NULL COMMENT '项目id',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建人id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建人名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` varchar(42) DEFAULT NULL COMMENT '修改人id',
  `update_name` varchar(100) DEFAULT NULL COMMENT '修改人名称',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `is_delete` char(1) DEFAULT '0' COMMENT '删除标识',
  `query_search` varchar(200) DEFAULT NULL COMMENT '搜索语句',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1532 DEFAULT CHARSET=utf8 COMMENT='分组';

-- ----------------------------
-- Table structure for p_group_condition
-- ----------------------------
DROP TABLE IF EXISTS `p_group_condition`;
CREATE TABLE `p_group_condition` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `uid` varchar(42) NOT NULL COMMENT '用户id',
  `project_id` varchar(42) DEFAULT NULL COMMENT '项目id',
  `group_id` varchar(42) DEFAULT NULL COMMENT '分组id',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建用户id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建用户名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` varchar(42) DEFAULT NULL COMMENT '修改用户id',
  `update_name` varchar(100) DEFAULT NULL COMMENT '修改用户名字',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `cort_type` int(4) DEFAULT NULL COMMENT '图形列表和患者列表区分字段',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3030011 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_group_data
-- ----------------------------
DROP TABLE IF EXISTS `p_group_data`;
CREATE TABLE `p_group_data` (
  `id` int(11) DEFAULT NULL COMMENT 'id',
  `group_id` varchar(50) NOT NULL COMMENT '分组id',
  `patient_sn` varchar(50) NOT NULL COMMENT '病人编号',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建用户id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建用户名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` varchar(42) DEFAULT NULL COMMENT '修改用户id',
  `update_name` varchar(100) DEFAULT NULL COMMENT '修改用户名字',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `remove` char(1) DEFAULT NULL COMMENT '移除判断',
  `patient_set_id` varchar(50) DEFAULT NULL,
  `efhnic` varchar(50) DEFAULT NULL,
  `nationality` varchar(50) DEFAULT NULL,
  `marital_status` varchar(50) DEFAULT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `patient_doc_id` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`group_id`,`patient_sn`),
  KEY `patient_sn` (`patient_sn`) USING BTREE,
  KEY `group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='分组数据表';

-- ----------------------------
-- Table structure for p_group_patient_data
-- ----------------------------
DROP TABLE IF EXISTS `p_group_patient_data`;
CREATE TABLE `p_group_patient_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `group_id` varchar(42) DEFAULT NULL COMMENT '分组id',
  `patient_set_id` varchar(42) DEFAULT NULL COMMENT '患者集ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1081 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_group_type
-- ----------------------------
DROP TABLE IF EXISTS `p_group_type`;
CREATE TABLE `p_group_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_type_id` char(3) DEFAULT NULL COMMENT '分组类型id',
  `group_type_name` varchar(100) DEFAULT NULL COMMENT '分组类型名字',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_input_task
-- ----------------------------
DROP TABLE IF EXISTS `p_input_task`;
CREATE TABLE `p_input_task` (
  `input_id` varchar(100) NOT NULL COMMENT '导入id',
  `project_id` varchar(52) DEFAULT NULL COMMENT '项目id',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目名称',
  `patient_set_id` varchar(255) DEFAULT NULL COMMENT '患者集id',
  `patient_set_name` varchar(255) DEFAULT NULL COMMENT '患者集名称',
  `uid` varchar(52) DEFAULT NULL COMMENT '用户id',
  `patient_count` int(11) DEFAULT NULL COMMENT '患者数量',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `status` tinyint(11) DEFAULT NULL COMMENT '状态',
  `remain_time` int(11) DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间 排序字段',
  `progress` tinyint(11) DEFAULT NULL COMMENT '完成度',
  `crf_id` varchar(255) DEFAULT NULL,
  `crf_name` varchar(255) DEFAULT NULL,
  `es_json` longtext,
  `uql_query` longtext,
  PRIMARY KEY (`input_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_oper_logs
-- ----------------------------
DROP TABLE IF EXISTS `p_oper_logs`;
CREATE TABLE `p_oper_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` varchar(42) DEFAULT NULL COMMENT '项目ID',
  `content` text COMMENT '内容',
  `is_delete` char(1) DEFAULT NULL COMMENT '删除标识',
  `url` varchar(40) DEFAULT NULL COMMENT 'url',
  `create_id` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建人名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13079 DEFAULT CHARSET=utf8 COMMENT='操作日志表';

-- ----------------------------
-- Table structure for p_patients_set
-- ----------------------------
DROP TABLE IF EXISTS `p_patients_set`;
CREATE TABLE `p_patients_set` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'int',
  `project_id` varchar(42) DEFAULT NULL COMMENT '项目id',
  `patients_set_id` varchar(42) NOT NULL COMMENT '患者集id',
  `patients_set_name` varchar(100) DEFAULT NULL COMMENT '患者集名称',
  `patients_set_describe` text COMMENT '患者集描述',
  `patients_count` int(11) DEFAULT NULL COMMENT '患者总数',
  `search_condition_id` varchar(42) DEFAULT NULL COMMENT '搜索条件id',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建人id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建人名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` varchar(42) DEFAULT NULL COMMENT '修改人id',
  `update_name` varchar(100) DEFAULT NULL COMMENT '修改人名字',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `is_delete` char(1) DEFAULT '0' COMMENT '删除标识',
  `uql_query` longtext COMMENT 'uql条件',
  `is_flush` int(11) DEFAULT NULL COMMENT '是否需要刷新',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1259 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_permission
-- ----------------------------
DROP TABLE IF EXISTS `p_permission`;
CREATE TABLE `p_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `permission_id` varchar(42) NOT NULL COMMENT '权限编码',
  `permission_name` varchar(100) DEFAULT NULL COMMENT '权限名称',
  `permission_type` char(3) DEFAULT NULL COMMENT '权限类型',
  `permission_url` varchar(50) DEFAULT NULL COMMENT '访问地址',
  `status` varchar(100) DEFAULT NULL COMMENT '状态',
  `permission_des` varchar(50) DEFAULT NULL COMMENT '权限描述',
  `lab_name` varchar(100) DEFAULT NULL COMMENT '所属科室名称',
  `is_delete` char(1) DEFAULT NULL COMMENT '删除标识',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建用户id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建用户名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` varchar(42) DEFAULT NULL COMMENT '修改用户id',
  `update_name` varchar(100) DEFAULT NULL COMMENT '修改用户名字',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_project
-- ----------------------------
DROP TABLE IF EXISTS `p_project`;
CREATE TABLE `p_project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` varchar(50) NOT NULL COMMENT '项目编码',
  `project_name` varchar(100) DEFAULT NULL COMMENT '项目名称',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '预计结束时间',
  `head_id` varchar(50) DEFAULT NULL COMMENT '负责人ID',
  `head_name` varchar(100) DEFAULT NULL COMMENT '负责人名称',
  `cooper_is` char(1) DEFAULT NULL COMMENT '是否合作项目',
  `cooper_id` varchar(50) DEFAULT NULL COMMENT '合作单位编码',
  `cooper_name` varchar(100) DEFAULT NULL COMMENT '合作单位名称',
  `cooper_head_id` varchar(50) DEFAULT NULL COMMENT '合作负责人编码',
  `cooper_head_name` varchar(100) DEFAULT NULL COMMENT '合作负责人名称',
  `projectdesc` text COMMENT '项目描述',
  `is_delete` char(1) DEFAULT NULL COMMENT '删除标识',
  `creator_id` varchar(100) DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(100) DEFAULT NULL COMMENT '创建人名称',
  `creator_time` datetime DEFAULT NULL COMMENT '项目创建时间',
  `modify_id` varchar(50) DEFAULT NULL COMMENT '修改人ID',
  `modify_name` varchar(100) DEFAULT NULL COMMENT '修改人名称',
  `modify_time` datetime DEFAULT NULL COMMENT '项目修改时间',
  `data_source` varchar(20) DEFAULT NULL COMMENT '数据源',
  `crf_id` varchar(42) DEFAULT NULL COMMENT '单病种id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=685 DEFAULT CHARSET=utf8 COMMENT='项目信息表';

-- ----------------------------
-- Table structure for p_project_member
-- ----------------------------
DROP TABLE IF EXISTS `p_project_member`;
CREATE TABLE `p_project_member` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uid` varchar(42) NOT NULL COMMENT '成员编码',
  `uname` varchar(100) DEFAULT NULL COMMENT '成员名称',
  `unumber` varchar(100) DEFAULT NULL COMMENT '成员账号',
  `lab_id` varchar(50) DEFAULT NULL COMMENT '所属医院编码',
  `lab_name` varchar(100) DEFAULT NULL COMMENT '所属医院名称',
  `org_id` varchar(50) DEFAULT NULL COMMENT '所属科室编码',
  `org_name` varchar(100) DEFAULT NULL COMMENT '所属科室名称',
  `is_delete` char(1) DEFAULT NULL COMMENT '删除标识',
  `create_id` varchar(42) DEFAULT NULL COMMENT '创建用户id',
  `create_name` varchar(100) DEFAULT NULL COMMENT '创建用户名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_id` varchar(42) DEFAULT NULL COMMENT '修改用户id',
  `update_name` varchar(100) DEFAULT NULL COMMENT '修改用户名字',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uid` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=1380 DEFAULT CHARSET=utf8 COMMENT='项目成员表';

-- ----------------------------
-- Table structure for p_project_scientific_map
-- ----------------------------
DROP TABLE IF EXISTS `p_project_scientific_map`;
CREATE TABLE `p_project_scientific_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scientific_id` char(3) NOT NULL COMMENT '科研id',
  `project_id` varchar(42) DEFAULT NULL COMMENT '项目id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=684 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_project_user_map
-- ----------------------------
DROP TABLE IF EXISTS `p_project_user_map`;
CREATE TABLE `p_project_user_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` varchar(42) DEFAULT NULL COMMENT '项目id',
  `uid` varchar(42) DEFAULT NULL COMMENT '用户id',
  `oblig_id` varchar(12) DEFAULT NULL COMMENT '职责人编码',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `project_id` (`project_id`,`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=1551 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_responsibility
-- ----------------------------
DROP TABLE IF EXISTS `p_responsibility`;
CREATE TABLE `p_responsibility` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `responsibility_id` char(3) DEFAULT NULL COMMENT '职责id',
  `responsibility_name` varchar(50) DEFAULT NULL COMMENT '职责人名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_role
-- ----------------------------
DROP TABLE IF EXISTS `p_role`;
CREATE TABLE `p_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` varchar(42) NOT NULL COMMENT '角色id',
  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名称',
  `role_desc` text COMMENT '角色描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `p_role_permission`;
CREATE TABLE `p_role_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` varchar(32) DEFAULT NULL COMMENT '角色ID',
  `permission_id` varchar(32) DEFAULT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_role_user_map
-- ----------------------------
DROP TABLE IF EXISTS `p_role_user_map`;
CREATE TABLE `p_role_user_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `role_id` varchar(42) DEFAULT NULL COMMENT '角色id',
  `uid` varchar(42) DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_scientific_group_map
-- ----------------------------
DROP TABLE IF EXISTS `p_scientific_group_map`;
CREATE TABLE `p_scientific_group_map` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scientific_id` int(11) DEFAULT NULL COMMENT '科研id',
  `group_type_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_scientific_research_type
-- ----------------------------
DROP TABLE IF EXISTS `p_scientific_research_type`;
CREATE TABLE `p_scientific_research_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `scientific_id` char(3) DEFAULT NULL,
  `scientific_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for p_search_log
-- ----------------------------
DROP TABLE IF EXISTS `p_search_log`;
CREATE TABLE `p_search_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `search_conditio` text COMMENT '搜索条件',
  `patient_set_id` varchar(42) DEFAULT NULL COMMENT '患者集id',
  `create_id` varchar(42) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1090 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Records of p_scientific_research_type
-- ----------------------------
INSERT INTO `p_scientific_research_type` VALUES ('1', '001', '描述性研究');
INSERT INTO `p_scientific_research_type` VALUES ('2', '002', '病例对照研究');
INSERT INTO `p_scientific_research_type` VALUES ('3', '003', '回顾性队列研究');

-- ----------------------------
-- Records of p_scientific_group_map
-- ----------------------------
INSERT INTO `p_scientific_group_map` VALUES ('1', '1', '1');
INSERT INTO `p_scientific_group_map` VALUES ('2', '2', '1');
INSERT INTO `p_scientific_group_map` VALUES ('3', '2', '2');
INSERT INTO `p_scientific_group_map` VALUES ('4', '3', '3');
INSERT INTO `p_scientific_group_map` VALUES ('5', '3', '4');

-- ----------------------------
-- Records of p_role
-- ----------------------------
INSERT INTO `p_role` VALUES ('1', '001', '创建人', '1');

-- ----------------------------
-- Records of p_responsibility
-- ----------------------------
INSERT INTO `p_responsibility` VALUES ('1', '001', '创建人');
INSERT INTO `p_responsibility` VALUES ('2', '002', '负责人');
INSERT INTO `p_responsibility` VALUES ('3', '003', '参与人');

-- ----------------------------
-- Records of p_group_type
-- ----------------------------
INSERT INTO `p_group_type` VALUES ('1', '001', '病例组');
INSERT INTO `p_group_type` VALUES ('2', '002', '对照组');
INSERT INTO `p_group_type` VALUES ('3', '003', '暴露组');
INSERT INTO `p_group_type` VALUES ('4', '004', '非暴露组');
