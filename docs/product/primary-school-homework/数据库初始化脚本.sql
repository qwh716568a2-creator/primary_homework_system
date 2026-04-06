SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `primary_homework_system`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `primary_homework_system`;

CREATE TABLE IF NOT EXISTS `organization_school` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_name` VARCHAR(128) NOT NULL COMMENT '学校名称',
  `school_code` VARCHAR(64) DEFAULT NULL COMMENT '学校编码',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_code` (`school_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学校表';

CREATE TABLE IF NOT EXISTS `organization_grade` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_id` BIGINT NOT NULL COMMENT '学校ID',
  `grade_name` VARCHAR(64) NOT NULL COMMENT '年级名称',
  `school_year` VARCHAR(16) DEFAULT NULL COMMENT '学年',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_grade_school_id` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='年级表';

CREATE TABLE IF NOT EXISTS `organization_class` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_id` BIGINT NOT NULL COMMENT '学校ID',
  `grade_id` BIGINT NOT NULL COMMENT '年级ID',
  `class_name` VARCHAR(64) NOT NULL COMMENT '班级名称',
  `class_code` VARCHAR(64) DEFAULT NULL COMMENT '班级编码',
  `homeroom_teacher_id` BIGINT DEFAULT NULL COMMENT '班主任用户ID',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_class_code` (`class_code`),
  KEY `idx_class_school_grade` (`school_id`, `grade_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='班级表';

CREATE TABLE IF NOT EXISTS `user_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `login_name` VARCHAR(64) DEFAULT NULL COMMENT '管理员登录名，教师学生家长为空',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码密文',
  `user_name` VARCHAR(64) NOT NULL COMMENT '用户名',
  `role_type` VARCHAR(32) NOT NULL COMMENT '角色类型 teacher student parent admin',
  `school_id` BIGINT DEFAULT NULL COMMENT '所属学校ID',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_login_name` (`login_name`),
  KEY `idx_user_role_school` (`role_type`, `school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统一账户表';

CREATE TABLE IF NOT EXISTS `student_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_user_id` BIGINT NOT NULL COMMENT '学生账户ID',
  `school_id` BIGINT NOT NULL COMMENT '学校ID',
  `grade_id` BIGINT NOT NULL COMMENT '年级ID',
  `class_id` BIGINT NOT NULL COMMENT '班级ID',
  `student_no` VARCHAR(64) NOT NULL COMMENT '学号，学生登录标识',
  `gender` VARCHAR(16) DEFAULT NULL COMMENT '性别',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_user_id` (`student_user_id`),
  UNIQUE KEY `uk_student_school_no` (`school_id`, `student_no`),
  KEY `idx_student_class_id` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生资料表';

CREATE TABLE IF NOT EXISTS `teacher_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_user_id` BIGINT NOT NULL COMMENT '教师账户ID',
  `school_id` BIGINT NOT NULL COMMENT '学校ID',
  `teacher_no` VARCHAR(64) DEFAULT NULL COMMENT '教师工号',
  `mobile` VARCHAR(20) NOT NULL COMMENT '教师手机号，登录标识',
  `gender` VARCHAR(16) DEFAULT NULL COMMENT '性别',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_user_id` (`teacher_user_id`),
  UNIQUE KEY `uk_teacher_mobile` (`mobile`),
  UNIQUE KEY `uk_teacher_school_no` (`school_id`, `teacher_no`),
  KEY `idx_teacher_profile_school` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师资料表';

CREATE TABLE IF NOT EXISTS `parent_profile` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_user_id` BIGINT NOT NULL COMMENT '家长账户ID',
  `school_id` BIGINT NOT NULL COMMENT '学校ID',
  `mobile` VARCHAR(20) NOT NULL COMMENT '家长手机号，登录标识',
  `gender` VARCHAR(16) DEFAULT NULL COMMENT '性别',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_user_id` (`parent_user_id`),
  UNIQUE KEY `uk_parent_mobile` (`mobile`),
  KEY `idx_parent_profile_school` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家长资料表';

CREATE TABLE IF NOT EXISTS `teacher_class_subject_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_id` BIGINT NOT NULL COMMENT '教师账户ID',
  `class_id` BIGINT NOT NULL COMMENT '班级ID',
  `subject_code` VARCHAR(32) NOT NULL COMMENT '学科编码',
  `is_head_teacher` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否班主任',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_class_subject` (`teacher_id`, `class_id`, `subject_code`),
  KEY `idx_teacher_class` (`class_id`, `teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师班级学科关系表';

CREATE TABLE IF NOT EXISTS `parent_student_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_user_id` BIGINT NOT NULL COMMENT '家长账户ID',
  `student_id` BIGINT NOT NULL COMMENT '学生资料ID',
  `relation_type` VARCHAR(32) NOT NULL COMMENT '关系类型',
  `is_primary` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主联系人',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_student` (`parent_user_id`, `student_id`),
  KEY `idx_parent_student_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家长学生绑定关系表';

CREATE TABLE IF NOT EXISTS `homework` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_id` BIGINT NOT NULL COMMENT '学校ID',
  `creator_teacher_id` BIGINT NOT NULL COMMENT '发布教师账户ID',
  `subject_code` VARCHAR(32) NOT NULL COMMENT '学科编码',
  `title` VARCHAR(128) NOT NULL COMMENT '作业标题',
  `content_text` TEXT DEFAULT NULL COMMENT '作业内容',
  `deadline_at` DATETIME NOT NULL COMMENT '截止时间',
  `allow_late_submit` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许逾期提交',
  `allow_resubmit` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否允许重复提交',
  `submit_type_mask` VARCHAR(64) NOT NULL COMMENT '提交方式组合 text,image,file',
  `need_parent_confirm` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要家长确认',
  `status` VARCHAR(32) NOT NULL DEFAULT 'draft' COMMENT '状态 draft published revoked closed',
  `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_homework_teacher_status` (`creator_teacher_id`, `status`),
  KEY `idx_homework_deadline` (`deadline_at`),
  KEY `idx_homework_school_subject` (`school_id`, `subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作业主表';

CREATE TABLE IF NOT EXISTS `homework_attachment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `homework_id` BIGINT NOT NULL COMMENT '作业ID',
  `asset_type` VARCHAR(32) NOT NULL COMMENT '文件类型 image file',
  `asset_url` VARCHAR(512) NOT NULL COMMENT '文件地址',
  `asset_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名称',
  `asset_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `sort_no` INT NOT NULL DEFAULT 1 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_homework_attachment_homework` (`homework_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作业附件表';

CREATE TABLE IF NOT EXISTS `homework_class_rel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `homework_id` BIGINT NOT NULL COMMENT '作业ID',
  `class_id` BIGINT NOT NULL COMMENT '班级ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_homework_class` (`homework_id`, `class_id`),
  KEY `idx_homework_class_rel_class` (`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作业班级关系表';

CREATE TABLE IF NOT EXISTS `student_homework_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `homework_id` BIGINT NOT NULL COMMENT '作业ID',
  `student_id` BIGINT NOT NULL COMMENT '学生资料ID',
  `class_id` BIGINT NOT NULL COMMENT '班级ID',
  `task_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '任务状态 pending submitted revision_required completed overdue',
  `latest_submission_id` BIGINT DEFAULT NULL COMMENT '最新提交ID',
  `submission_count` INT NOT NULL DEFAULT 0 COMMENT '提交次数',
  `latest_submitted_at` DATETIME DEFAULT NULL COMMENT '最近提交时间',
  `latest_review_status` VARCHAR(32) NOT NULL DEFAULT 'unreviewed' COMMENT '最近批改状态',
  `latest_reviewed_at` DATETIME DEFAULT NULL COMMENT '最近批改时间',
  `is_late` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否逾期',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_homework_student` (`homework_id`, `student_id`),
  KEY `idx_task_class_status` (`class_id`, `task_status`),
  KEY `idx_task_student` (`student_id`, `task_status`),
  KEY `idx_task_latest_submission` (`latest_submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生作业任务表';

CREATE TABLE IF NOT EXISTS `homework_submission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` BIGINT NOT NULL COMMENT '学生作业任务ID',
  `homework_id` BIGINT NOT NULL COMMENT '作业ID',
  `student_id` BIGINT NOT NULL COMMENT '学生资料ID',
  `operator_role` VARCHAR(32) NOT NULL COMMENT '操作人角色 student parent',
  `operator_user_id` BIGINT NOT NULL COMMENT '操作人账户ID',
  `submit_text` TEXT DEFAULT NULL COMMENT '提交说明',
  `submitted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `is_late` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否逾期',
  `version_no` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  `submit_status` VARCHAR(32) NOT NULL DEFAULT 'submitted' COMMENT '提交状态 submitted withdrawn',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_submission_task` (`task_id`, `version_no`),
  KEY `idx_submission_student` (`student_id`, `submitted_at`),
  KEY `idx_submission_homework` (`homework_id`, `submitted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作业提交表';

CREATE TABLE IF NOT EXISTS `homework_submission_asset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `submission_id` BIGINT NOT NULL COMMENT '提交ID',
  `asset_type` VARCHAR(32) NOT NULL COMMENT '文件类型 image file',
  `asset_url` VARCHAR(512) NOT NULL COMMENT '文件地址',
  `asset_name` VARCHAR(255) DEFAULT NULL COMMENT '文件名称',
  `asset_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `sort_no` INT NOT NULL DEFAULT 1 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_submission_asset_submission` (`submission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='提交附件表';

CREATE TABLE IF NOT EXISTS `homework_review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` BIGINT NOT NULL COMMENT '学生作业任务ID',
  `homework_id` BIGINT NOT NULL COMMENT '作业ID',
  `student_id` BIGINT NOT NULL COMMENT '学生资料ID',
  `submission_id` BIGINT NOT NULL COMMENT '对应提交ID',
  `reviewer_teacher_id` BIGINT NOT NULL COMMENT '批改教师账户ID',
  `review_status` VARCHAR(32) NOT NULL COMMENT '批改状态 completed revision_required',
  `score` DECIMAL(5,2) DEFAULT NULL COMMENT '分数',
  `score_level` VARCHAR(16) DEFAULT NULL COMMENT '等级',
  `comment_text` TEXT DEFAULT NULL COMMENT '评语',
  `reviewed_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '批改时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_task` (`task_id`, `reviewed_at`),
  KEY `idx_review_submission` (`submission_id`),
  KEY `idx_review_teacher` (`reviewer_teacher_id`, `reviewed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='作业批改表';

CREATE TABLE IF NOT EXISTS `homework_review_asset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `review_id` BIGINT NOT NULL COMMENT '批改记录ID',
  `asset_type` VARCHAR(32) NOT NULL DEFAULT 'image' COMMENT '文件类型',
  `asset_url` VARCHAR(512) NOT NULL COMMENT '文件地址',
  `sort_no` INT NOT NULL DEFAULT 1 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_asset_review` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='批改附件表';

CREATE TABLE IF NOT EXISTS `notification_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型 homework_publish deadline_reminder submission_reminder review_result',
  `biz_id` BIGINT NOT NULL COMMENT '业务主键ID',
  `receiver_user_id` BIGINT NOT NULL COMMENT '接收人账户ID',
  `receiver_role` VARCHAR(32) NOT NULL COMMENT '接收人角色',
  `notify_channel` VARCHAR(32) NOT NULL COMMENT '通知渠道 in_app wechat sms',
  `notify_title` VARCHAR(128) NOT NULL COMMENT '通知标题',
  `notify_content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `send_status` VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '发送状态 pending success failed',
  `sent_at` DATETIME DEFAULT NULL COMMENT '发送时间',
  `read_at` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_notification_receiver` (`receiver_user_id`, `send_status`, `created_at`),
  KEY `idx_notification_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知记录表';

CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operator_user_id` BIGINT NOT NULL COMMENT '操作人账户ID',
  `operator_role` VARCHAR(32) NOT NULL COMMENT '操作人角色',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型',
  `biz_id` BIGINT NOT NULL COMMENT '业务ID',
  `action_type` VARCHAR(64) NOT NULL COMMENT '操作类型',
  `request_payload` JSON DEFAULT NULL COMMENT '请求快照',
  `result_code` INT DEFAULT NULL COMMENT '结果码',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_operator` (`operator_user_id`, `created_at`),
  KEY `idx_operation_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

SET FOREIGN_KEY_CHECKS = 1;
