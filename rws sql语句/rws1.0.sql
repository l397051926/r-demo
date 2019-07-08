/*
Navicat MySQL Data Transfer

Source Server         : 10.0.0.68
Source Server Version : 50712
Source Host           : 10.0.0.68:3306
Source Database       : rws

Target Server Type    : MYSQL
Target Server Version : 50712
File Encoding         : 65001

Date: 2017-11-14 10:39:45
*/

SET FOREIGN_KEY_CHECKS=0;
create database rws DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
use rws;
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
  `sort_key` varchar(60) DEFAULT NULL,
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
  `index_type_desc` varchar(20) DEFAULT NULL,
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
  PRIMARY KEY (`id`)
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
  `ref_relation` varchar(10) DEFAULT NULL COMMENT '关联关系 direct, ref, leftref',
  `logic_sing` varchar(8) DEFAULT NULL COMMENT '逻辑运算符',
  `ref_active_id` varchar(64) DEFAULT NULL COMMENT '引用的id',
  `value` varchar(100) DEFAULT NULL COMMENT '值',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '父级id',
  `ref_active_name` varchar(100) DEFAULT NULL COMMENT '引用的活动名称',
  `type` tinyint(4) DEFAULT NULL COMMENT '1:组别;2:条件',
  `level` int(11) DEFAULT NULL,
  `need_path` varchar(255) DEFAULT NULL,
  `condition_type` tinyint(4) DEFAULT NULL COMMENT '条件类型，1：指标条件，2：事件条件，3：入组条件，4：排除条件',
  `uuid` varchar(70) DEFAULT NULL COMMENT 'webui前端回填数据使用',
  PRIMARY KEY (`id`)
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
