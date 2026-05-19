USE `primary_homework`;

START TRANSACTION;

-- 1. 统一学生学号唯一索引为 school_id + student_no
SET @drop_old_idx_sql = (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'user_student'
        AND index_name = 'uk_student_class_no'
    )
    THEN 'ALTER TABLE `user_student` DROP INDEX `uk_student_class_no`'
    ELSE 'SELECT 1'
  END
);
PREPARE stmt FROM @drop_old_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_new_idx_sql = (
  SELECT CASE
    WHEN EXISTS (
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'user_student'
        AND index_name = 'uk_student_school_no'
    )
    THEN 'SELECT 1'
    ELSE 'ALTER TABLE `user_student` ADD UNIQUE KEY `uk_student_school_no` (`school_id`, `student_no`)'
  END
);
PREPARE stmt FROM @add_new_idx_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 删除并重建统一账户表、教师资料表、家长资料表
DROP TABLE IF EXISTS `user_teacher`;
DROP TABLE IF EXISTS `user_parent`;
DROP TABLE IF EXISTS `user_account`;

CREATE TABLE `user_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `login_name` VARCHAR(64) DEFAULT NULL COMMENT '登录名，仅管理员使用',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码密文',
  `user_name` VARCHAR(64) NOT NULL COMMENT '用户姓名',
  `role_type` VARCHAR(32) NOT NULL COMMENT '角色类型：teacher student parent admin',
  `school_id` BIGINT DEFAULT NULL COMMENT '所属学校ID',
  `status` VARCHAR(32) NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_login_name` (`login_name`),
  KEY `idx_user_role_school` (`role_type`, `school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='统一账户表';

CREATE TABLE `user_teacher` (
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
  KEY `idx_user_teacher_school` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='教师资料表';

CREATE TABLE `user_parent` (
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
  KEY `idx_user_parent_school` (`school_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家长资料表';

-- 3. 重新插入统一账户和角色资料测试数据
INSERT INTO `user_account`
(`id`, `login_name`, `password_hash`, `user_name`, `role_type`, `school_id`, `status`, `last_login_at`, `created_at`, `updated_at`)
VALUES
(10001, 'admin', '123456', '系统管理员', 'admin', 1, 'enabled', '2026-04-02 08:00:00', '2026-03-01 09:00:00', '2026-04-02 08:00:00'),
(10002, NULL, '123456', '张丽', 'teacher', 1, 'enabled', '2026-04-02 07:50:00', '2026-03-01 09:10:00', '2026-04-02 07:50:00'),
(10003, NULL, '123456', '陈晨', 'teacher', 1, 'enabled', '2026-04-02 08:05:00', '2026-03-01 09:10:00', '2026-04-02 08:05:00'),
(10004, NULL, '123456', '王敏', 'teacher', 1, 'enabled', '2026-04-02 08:10:00', '2026-03-01 09:10:00', '2026-04-02 08:10:00'),
(20001, NULL, '123456', '杨乐乐', 'student', 1, 'enabled', '2026-04-02 19:15:00', '2026-03-02 10:00:00', '2026-04-02 19:15:00'),
(20002, NULL, '123456', '周子轩', 'student', 1, 'enabled', '2026-04-02 19:25:00', '2026-03-02 10:00:00', '2026-04-02 19:25:00'),
(20003, NULL, '123456', '陈雨桐', 'student', 1, 'enabled', '2026-04-02 18:30:00', '2026-03-02 10:00:00', '2026-04-02 18:30:00'),
(20004, NULL, '123456', '李浩然', 'student', 1, 'enabled', '2026-04-02 17:40:00', '2026-03-02 10:00:00', '2026-04-02 17:40:00'),
(20005, NULL, '123456', '孙一诺', 'student', 1, 'enabled', '2026-04-02 17:00:00', '2026-03-02 10:00:00', '2026-04-02 17:00:00'),
(20006, NULL, '123456', '赵思远', 'student', 1, 'enabled', '2026-04-02 08:30:00', '2026-03-02 10:00:00', '2026-04-02 08:30:00'),
(30001, NULL, '123456', '杨乐乐妈妈', 'parent', 1, 'enabled', '2026-04-02 19:16:00', '2026-03-02 10:30:00', '2026-04-02 19:16:00'),
(30002, NULL, '123456', '周子轩爸爸', 'parent', 1, 'enabled', '2026-04-02 19:26:00', '2026-03-02 10:30:00', '2026-04-02 19:26:00'),
(30003, NULL, '123456', '陈雨桐妈妈', 'parent', 1, 'enabled', '2026-04-02 18:31:00', '2026-03-02 10:30:00', '2026-04-02 18:31:00'),
(30004, NULL, '123456', '李浩然妈妈', 'parent', 1, 'enabled', '2026-04-02 17:41:00', '2026-03-02 10:30:00', '2026-04-02 17:41:00'),
(30005, NULL, '123456', '孙一诺爸爸', 'parent', 1, 'enabled', '2026-04-02 17:01:00', '2026-03-02 10:30:00', '2026-04-02 17:01:00'),
(30006, NULL, '123456', '赵思远妈妈', 'parent', 1, 'enabled', '2026-04-02 08:31:00', '2026-03-02 10:30:00', '2026-04-02 08:31:00');

INSERT INTO `user_teacher`
(`id`, `teacher_user_id`, `school_id`, `teacher_no`, `mobile`, `gender`, `created_at`, `updated_at`)
VALUES
(41001, 10002, 1, 'T301001', '13900000002', 'female', '2026-03-01 09:20:00', '2026-03-01 09:20:00'),
(41002, 10003, 1, 'T301002', '13900000003', 'female', '2026-03-01 09:20:00', '2026-03-01 09:20:00'),
(41003, 10004, 1, 'T401001', '13900000004', 'female', '2026-03-01 09:20:00', '2026-03-01 09:20:00');

INSERT INTO `user_parent`
(`id`, `parent_user_id`, `school_id`, `mobile`, `gender`, `created_at`, `updated_at`)
VALUES
(42001, 30001, 1, '13710000001', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00'),
(42002, 30002, 1, '13710000002', 'male', '2026-03-02 10:40:00', '2026-03-02 10:40:00'),
(42003, 30003, 1, '13710000003', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00'),
(42004, 30004, 1, '13710000004', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00'),
(42005, 30005, 1, '13710000005', 'male', '2026-03-02 10:40:00', '2026-03-02 10:40:00'),
(42006, 30006, 1, '13710000006', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00');

COMMIT;
