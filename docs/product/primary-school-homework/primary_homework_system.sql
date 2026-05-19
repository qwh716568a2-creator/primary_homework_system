/*
 Navicat MySQL Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : primary_homework_system

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 12/04/2026 17:28:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for homework
-- ----------------------------
DROP TABLE IF EXISTS `homework`;
CREATE TABLE `homework`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `creator_teacher_id` bigint NOT NULL COMMENT '发布教师ID',
  `subject_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学科编码',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '作业标题',
  `content_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '作业内容',
  `deadline_at` datetime NOT NULL COMMENT '截止时间',
  `allow_late_submit` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许逾期提交',
  `allow_resubmit` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否允许重复提交',
  `submit_type_mask` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '提交方式组合 text,image,file',
  `need_parent_confirm` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否需要家长确认',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'draft' COMMENT '状态 draft published revoked closed',
  `published_at` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_homework_teacher_status`(`creator_teacher_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_homework_deadline`(`deadline_at` ASC) USING BTREE,
  INDEX `idx_homework_school_subject`(`school_id` ASC, `subject_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50006 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作业主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework
-- ----------------------------
INSERT INTO `homework` VALUES (50001, 1, 10002, 'math', '数学口算练习', '完成课后练习第3、4页，拍照上传。', '2026-04-03 20:00:00', 1, 1, 'text,image', 0, 'published', '2026-04-02 16:30:00', '2026-04-02 16:20:00', '2026-04-02 16:30:00');
INSERT INTO `homework` VALUES (50002, 1, 10003, 'chinese', '语文生字抄写', '抄写第8课生字每个3遍，并朗读课文。', '2026-04-02 18:00:00', 1, 1, 'text,image', 1, 'published', '2026-04-02 09:00:00', '2026-04-02 08:50:00', '2026-04-02 09:00:00');
INSERT INTO `homework` VALUES (50003, 1, 10004, 'english', '英语单词默写', '默写 Unit 3 单词并拍照上传。', '2026-04-01 18:00:00', 1, 1, 'image', 0, 'published', '2026-03-31 17:00:00', '2026-03-31 16:50:00', '2026-03-31 17:00:00');
INSERT INTO `homework` VALUES (50004, 1, 10002, 'math', '周末拓展题', '完成拓展练习单第1页，周末前完成。', '2026-04-05 20:00:00', 1, 1, 'text,file', 0, 'draft', NULL, '2026-04-02 18:00:00', '2026-04-02 18:00:00');

-- ----------------------------
-- Table structure for homework_attachment
-- ----------------------------
DROP TABLE IF EXISTS `homework_attachment`;
CREATE TABLE `homework_attachment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `asset_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件类型 image file',
  `asset_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件地址',
  `asset_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名称',
  `asset_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `sort_no` int NOT NULL DEFAULT 1 COMMENT '排序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_homework_attachment_homework`(`homework_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 60004 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作业附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_attachment
-- ----------------------------
INSERT INTO `homework_attachment` VALUES (60001, 50001, 'file', 'https://oss.example.com/homework/20260402/math-oral.pdf', '数学口算练习.pdf', 102400, 1, '2026-04-02 16:21:00');
INSERT INTO `homework_attachment` VALUES (60002, 50002, 'image', 'https://oss.example.com/homework/20260402/chinese-demo.jpg', '生字示例.jpg', 204800, 1, '2026-04-02 08:55:00');
INSERT INTO `homework_attachment` VALUES (60003, 50003, 'file', 'https://oss.example.com/homework/20260331/english-word-list.pdf', '英语单词表.pdf', 51200, 1, '2026-03-31 16:55:00');

-- ----------------------------
-- Table structure for homework_class_rel
-- ----------------------------
DROP TABLE IF EXISTS `homework_class_rel`;
CREATE TABLE `homework_class_rel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_homework_class`(`homework_id` ASC, `class_id` ASC) USING BTREE,
  INDEX `idx_homework_class_rel_class`(`class_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70006 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作业班级关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_class_rel
-- ----------------------------
INSERT INTO `homework_class_rel` VALUES (70001, 50001, 101, '2026-04-02 16:30:00');
INSERT INTO `homework_class_rel` VALUES (70002, 50002, 102, '2026-04-02 09:00:00');
INSERT INTO `homework_class_rel` VALUES (70003, 50003, 201, '2026-03-31 17:00:00');
INSERT INTO `homework_class_rel` VALUES (70004, 50004, 101, '2026-04-02 18:00:00');

-- ----------------------------
-- Table structure for homework_review
-- ----------------------------
DROP TABLE IF EXISTS `homework_review`;
CREATE TABLE `homework_review`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint NOT NULL COMMENT '学生作业任务ID',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `student_id` bigint NOT NULL COMMENT '学生档案ID',
  `submission_id` bigint NOT NULL COMMENT '对应提交ID',
  `reviewer_teacher_id` bigint NOT NULL COMMENT '批改教师ID',
  `review_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '批改状态 completed revision_required',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '分数',
  `score_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级',
  `comment_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '评语',
  `reviewed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '批改时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_task`(`task_id` ASC, `reviewed_at` ASC) USING BTREE,
  INDEX `idx_review_submission`(`submission_id` ASC) USING BTREE,
  INDEX `idx_review_teacher`(`reviewer_teacher_id` ASC, `reviewed_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 92006 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作业批改表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_review
-- ----------------------------
INSERT INTO `homework_review` VALUES (92001, 80001, 50001, 40001, 90001, 10002, 'completed', 95.00, 'A', '完成很好，书写整洁。', '2026-04-02 20:30:00', '2026-04-02 20:30:00');
INSERT INTO `homework_review` VALUES (92002, 80002, 50001, 40002, 90002, 10002, 'revision_required', 78.00, 'B', '第4页有两道题计算错误，请订正后重新提交。', '2026-04-02 20:40:00', '2026-04-02 20:40:00');
INSERT INTO `homework_review` VALUES (92003, 80006, 50003, 40006, 90004, 10004, 'completed', 88.00, 'B', '已补交，单词拼写基本正确。', '2026-04-02 09:00:00', '2026-04-02 09:00:00');
INSERT INTO `homework_review` VALUES (92004, 80004, 50002, 40004, 90003, 10003, 'completed', 90.00, 'A', '步骤完整，但书写和表达还可以更清晰。', '2026-04-10 16:12:39', '2026-04-10 16:12:38');
INSERT INTO `homework_review` VALUES (92005, 80002, 50001, 40002, 90002, 10002, 'revision_required', 88.00, 'B', '????????', '2026-04-12 11:20:31', '2026-04-12 11:20:31');

-- ----------------------------
-- Table structure for homework_review_asset
-- ----------------------------
DROP TABLE IF EXISTS `homework_review_asset`;
CREATE TABLE `homework_review_asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `review_id` bigint NOT NULL COMMENT '批改记录ID',
  `asset_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'image' COMMENT '文件类型',
  `asset_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件地址',
  `sort_no` int NOT NULL DEFAULT 1 COMMENT '排序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_asset_review`(`review_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 93002 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '批改附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_review_asset
-- ----------------------------
INSERT INTO `homework_review_asset` VALUES (93001, 92002, 'image', 'https://oss.example.com/review/20260402/92002-1.png', 1, '2026-04-02 20:40:10');

-- ----------------------------
-- Table structure for homework_submission
-- ----------------------------
DROP TABLE IF EXISTS `homework_submission`;
CREATE TABLE `homework_submission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint NOT NULL COMMENT '学生作业任务ID',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `student_id` bigint NOT NULL COMMENT '学生档案ID',
  `operator_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作人角色 student parent',
  `operator_user_id` bigint NOT NULL COMMENT '操作人用户ID',
  `submit_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '提交说明',
  `submitted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `is_late` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逾期',
  `version_no` int NOT NULL DEFAULT 1 COMMENT '版本号',
  `submit_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'submitted' COMMENT '提交状态 submitted withdrawn',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_task`(`task_id` ASC, `version_no` ASC) USING BTREE,
  INDEX `idx_submission_student`(`student_id` ASC, `submitted_at` ASC) USING BTREE,
  INDEX `idx_submission_homework`(`homework_id` ASC, `submitted_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 90008 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作业提交表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_submission
-- ----------------------------
INSERT INTO `homework_submission` VALUES (90001, 80001, 50001, 40001, 'student', 20001, '我已完成口算练习，请老师批改。', '2026-04-02 19:10:00', 0, 1, 'submitted', '2026-04-02 19:10:00');
INSERT INTO `homework_submission` VALUES (90002, 80002, 50001, 40002, 'parent', 30002, '孩子已完成，家长代为上传。', '2026-04-02 19:20:00', 0, 1, 'submitted', '2026-04-02 19:20:00');
INSERT INTO `homework_submission` VALUES (90003, 80004, 50002, 40004, 'student', 20004, '生字抄写已完成。', '2026-04-02 17:30:00', 0, 1, 'submitted', '2026-04-02 17:30:00');
INSERT INTO `homework_submission` VALUES (90004, 80006, 50003, 40006, 'parent', 30006, '昨天忘记提交，今天早上补交。', '2026-04-02 08:20:00', 1, 1, 'submitted', '2026-04-02 08:20:00');
INSERT INTO `homework_submission` VALUES (90005, 80001, 50001, 40001, 'student', 20001, '?????????', '2026-04-12 11:28:55', 1, 2, 'submitted', '2026-04-12 11:28:55');
INSERT INTO `homework_submission` VALUES (90006, 80001, 50001, 40001, 'parent', 30001, 'PARENT-ASCII-SUBMIT', '2026-04-12 11:29:49', 1, 3, 'submitted', '2026-04-12 11:29:49');
INSERT INTO `homework_submission` VALUES (90007, 80001, 50001, 40001, 'student', 20001, '学生中文提交测试', '2026-04-12 11:30:43', 1, 4, 'submitted', '2026-04-12 11:30:42');

-- ----------------------------
-- Table structure for homework_submission_asset
-- ----------------------------
DROP TABLE IF EXISTS `homework_submission_asset`;
CREATE TABLE `homework_submission_asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `submission_id` bigint NOT NULL COMMENT '提交ID',
  `asset_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件类型 image file',
  `asset_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件地址',
  `asset_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名称',
  `asset_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `sort_no` int NOT NULL DEFAULT 1 COMMENT '排序号',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_asset_submission`(`submission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 91005 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '提交附件表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of homework_submission_asset
-- ----------------------------
INSERT INTO `homework_submission_asset` VALUES (91001, 90001, 'image', 'https://oss.example.com/submission/20260402/90001-1.jpg', '口算作业-1.jpg', 256000, 1, '2026-04-02 19:10:10');
INSERT INTO `homework_submission_asset` VALUES (91002, 90002, 'image', 'https://oss.example.com/submission/20260402/90002-1.jpg', '数学作业-周子轩.jpg', 245000, 1, '2026-04-02 19:20:10');
INSERT INTO `homework_submission_asset` VALUES (91003, 90003, 'image', 'https://oss.example.com/submission/20260402/90003-1.jpg', '语文抄写-李浩然.jpg', 268000, 1, '2026-04-02 17:30:10');
INSERT INTO `homework_submission_asset` VALUES (91004, 90004, 'image', 'https://oss.example.com/submission/20260402/90004-1.jpg', '英语默写-赵思远.jpg', 251000, 1, '2026-04-02 08:20:10');

-- ----------------------------
-- Table structure for mobile_preference
-- ----------------------------
DROP TABLE IF EXISTS `mobile_preference`;
CREATE TABLE `mobile_preference`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'student parent',
  `master_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `assignment_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `review_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `reminder_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `system_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `sound_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `vibration_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `quiet_hours_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `quiet_start` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '22:00',
  `quiet_end` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '07:00',
  `hide_account_identifier` tinyint(1) NOT NULL DEFAULT 0,
  `remember_account` tinyint(1) NOT NULL DEFAULT 1,
  `login_alert_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `app_lock_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `biometric_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `password_checked_at` datetime NULL DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_mobile_preference_user_role`(`user_id` ASC, `role_type` ASC) USING BTREE,
  INDEX `idx_mobile_preference_role`(`role_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '小程序消息设置与账号安全设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mobile_preference
-- ----------------------------
INSERT INTO `mobile_preference` VALUES (2, 30002, 'parent', 1, 1, 1, 1, 1, 1, 1, 0, '22:00', '07:00', 0, 1, 1, 0, 0, '2026-04-11 12:28:06', '2026-04-11 12:27:29', '2026-04-11 12:27:29');
INSERT INTO `mobile_preference` VALUES (3, 20001, 'student', 1, 1, 1, 1, 1, 1, 1, 0, '22:00', '07:00', 0, 1, 1, 0, 0, '2026-04-12 11:31:11', '2026-04-12 11:28:55', '2026-04-12 11:28:55');
INSERT INTO `mobile_preference` VALUES (4, 30001, 'parent', 1, 1, 1, 1, 1, 1, 1, 0, '22:00', '07:00', 0, 1, 1, 0, 0, '2026-04-12 11:31:12', '2026-04-12 11:29:49', '2026-04-12 11:29:49');

-- ----------------------------
-- Table structure for notification_record
-- ----------------------------
DROP TABLE IF EXISTS `notification_record`;
CREATE TABLE `notification_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型 homework_publish deadline_reminder submission_reminder review_result',
  `biz_id` bigint NOT NULL COMMENT '业务主键ID',
  `receiver_user_id` bigint NOT NULL COMMENT '接收人用户ID',
  `receiver_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接收人角色',
  `notify_channel` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知渠道 in_app wechat sms',
  `notify_title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知标题',
  `notify_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知内容',
  `send_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '发送状态 pending success failed',
  `sent_at` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `read_at` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notification_receiver`(`receiver_user_id` ASC, `send_status` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_notification_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 94025 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '通知记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification_record
-- ----------------------------
INSERT INTO `notification_record` VALUES (94001, 'homework_publish', 50001, 20001, 'student', 'in_app', '新作业通知', '数学口算练习已发布，请于2026-04-03 20:00前完成。', 'success', '2026-04-02 16:31:00', '2026-04-02 18:50:00', '2026-04-02 16:31:00');
INSERT INTO `notification_record` VALUES (94002, 'homework_publish', 50001, 30001, 'parent', 'wechat', '新作业通知', '杨乐乐有新的数学作业，请协助提醒完成。', 'success', '2026-04-02 16:31:10', '2026-04-02 16:35:00', '2026-04-02 16:31:10');
INSERT INTO `notification_record` VALUES (94003, 'review_result', 92002, 20002, 'student', 'in_app', '批改结果通知', '数学口算练习已批改，老师要求你订正后重新提交。', 'success', '2026-04-02 20:41:00', NULL, '2026-04-02 20:41:00');
INSERT INTO `notification_record` VALUES (94004, 'review_result', 92002, 30002, 'parent', 'wechat', '批改结果通知', '周子轩的数学作业需要订正，请尽快查看。', 'success', '2026-04-02 20:41:10', NULL, '2026-04-02 20:41:10');
INSERT INTO `notification_record` VALUES (94005, 'submission_reminder', 50002, 30005, 'parent', 'wechat', '作业催交通知', '孙一诺的语文生字抄写已逾期未提交，请尽快完成。', 'success', '2026-04-02 18:20:00', NULL, '2026-04-02 18:20:00');
INSERT INTO `notification_record` VALUES (94006, 'review_result', 92003, 30006, 'parent', 'wechat', '批改结果通知', '赵思远的英语单词默写已批改，请查看老师反馈。', 'success', '2026-04-02 09:01:00', '2026-04-02 09:10:00', '2026-04-02 09:01:00');
INSERT INTO `notification_record` VALUES (94007, 'custom_notice', 95006, 20001, 'student', 'in_app', '1212', '1212', 'success', '2026-04-10 10:33:59', NULL, '2026-04-10 10:33:59');
INSERT INTO `notification_record` VALUES (94008, 'custom_notice', 95006, 20002, 'student', 'in_app', '1212', '1212', 'success', '2026-04-10 10:33:59', NULL, '2026-04-10 10:33:59');
INSERT INTO `notification_record` VALUES (94009, 'custom_notice', 95006, 20003, 'student', 'in_app', '1212', '1212', 'success', '2026-04-10 10:33:59', NULL, '2026-04-10 10:33:59');
INSERT INTO `notification_record` VALUES (94010, 'custom_notice', 95006, 30001, 'parent', 'in_app', '1212', '1212', 'success', '2026-04-10 10:33:59', NULL, '2026-04-10 10:33:59');
INSERT INTO `notification_record` VALUES (94011, 'custom_notice', 95006, 30002, 'parent', 'in_app', '1212', '1212', 'success', '2026-04-10 10:33:59', NULL, '2026-04-10 10:33:59');
INSERT INTO `notification_record` VALUES (94012, 'custom_notice', 95006, 30003, 'parent', 'in_app', '1212', '1212', 'success', '2026-04-10 10:33:59', NULL, '2026-04-10 10:33:59');
INSERT INTO `notification_record` VALUES (94013, 'homework_notice', 95007, 30001, 'parent', 'in_app', '????', '????????????', 'success', '2026-04-12 11:20:09', NULL, '2026-04-12 11:20:09');
INSERT INTO `notification_record` VALUES (94014, 'homework_notice', 95007, 30002, 'parent', 'in_app', '????', '????????????', 'success', '2026-04-12 11:20:09', NULL, '2026-04-12 11:20:09');
INSERT INTO `notification_record` VALUES (94015, 'homework_notice', 95007, 30003, 'parent', 'in_app', '????', '????????????', 'success', '2026-04-12 11:20:09', NULL, '2026-04-12 11:20:09');
INSERT INTO `notification_record` VALUES (94016, 'custom_notice', 95008, 30001, 'parent', 'in_app', '中文通知标题', '这是一条中文通知内容', 'success', '2026-04-12 11:30:57', NULL, '2026-04-12 11:30:57');
INSERT INTO `notification_record` VALUES (94017, 'custom_notice', 95008, 30002, 'parent', 'in_app', '中文通知标题', '这是一条中文通知内容', 'success', '2026-04-12 11:30:57', NULL, '2026-04-12 11:30:57');
INSERT INTO `notification_record` VALUES (94018, 'custom_notice', 95008, 30003, 'parent', 'in_app', '中文通知标题', '这是一条中文通知内容', 'success', '2026-04-12 11:30:57', NULL, '2026-04-12 11:30:57');
INSERT INTO `notification_record` VALUES (94019, 'custom_notice', 95009, 30001, 'parent', 'in_app', '回归kind测试2', '用于确认system kind', 'success', '2026-04-12 15:58:17', NULL, '2026-04-12 15:58:17');
INSERT INTO `notification_record` VALUES (94020, 'custom_notice', 95009, 30002, 'parent', 'in_app', '回归kind测试2', '用于确认system kind', 'success', '2026-04-12 15:58:17', NULL, '2026-04-12 15:58:17');
INSERT INTO `notification_record` VALUES (94021, 'custom_notice', 95009, 30003, 'parent', 'in_app', '回归kind测试2', '用于确认system kind', 'success', '2026-04-12 15:58:17', NULL, '2026-04-12 15:58:17');
INSERT INTO `notification_record` VALUES (94022, 'custom_notice', 95010, 30001, 'parent', 'in_app', '新版kind回归', '检查custom_notice是否映射system', 'success', '2026-04-12 16:13:53', NULL, '2026-04-12 16:13:53');
INSERT INTO `notification_record` VALUES (94023, 'custom_notice', 95010, 30002, 'parent', 'in_app', '新版kind回归', '检查custom_notice是否映射system', 'success', '2026-04-12 16:13:53', NULL, '2026-04-12 16:13:53');
INSERT INTO `notification_record` VALUES (94024, 'custom_notice', 95010, 30003, 'parent', 'in_app', '新版kind回归', '检查custom_notice是否映射system', 'success', '2026-04-12 16:13:53', NULL, '2026-04-12 16:13:53');

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `operator_user_id` bigint NOT NULL COMMENT '操作人用户ID',
  `operator_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作人角色',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型',
  `biz_id` bigint NOT NULL COMMENT '业务ID',
  `action_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作类型',
  `request_payload` json NULL COMMENT '请求快照',
  `result_code` int NULL DEFAULT NULL COMMENT '结果码',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_operator`(`operator_user_id` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_operation_biz`(`biz_type` ASC, `biz_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 95011 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (95001, 10002, 'teacher', 'homework', 50001, 'publish_homework', '{\"title\": \"数学口算练习\", \"classIds\": [101], \"deadlineAt\": \"2026-04-03 20:00:00\"}', 0, '2026-04-02 16:30:00');
INSERT INTO `operation_log` VALUES (95002, 20001, 'student', 'submission', 90001, 'submit_homework', '{\"taskId\": 80001, \"assetCount\": 1}', 0, '2026-04-02 19:10:00');
INSERT INTO `operation_log` VALUES (95003, 30002, 'parent', 'submission', 90002, 'submit_homework_for_child', '{\"taskId\": 80002, \"studentId\": 40002, \"assetCount\": 1}', 0, '2026-04-02 19:20:00');
INSERT INTO `operation_log` VALUES (95004, 10002, 'teacher', 'review', 92002, 'review_homework', '{\"taskId\": 80002, \"reviewStatus\": \"revision_required\"}', 0, '2026-04-02 20:40:00');
INSERT INTO `operation_log` VALUES (95005, 10003, 'teacher', 'notification', 94005, 'remind_pending_homework', '{\"classId\": 102, \"homeworkId\": 50002, \"remindType\": \"overdue\"}', 0, '2026-04-02 18:20:00');
INSERT INTO `operation_log` VALUES (95006, 10002, 'teacher', 'custom_notice', 95006, 'teacher_message_send', '{\"sentAt\": \"2026-04-10 10:33\", \"classIds\": [101], \"scopeType\": \"class\", \"classNames\": [\"三年级(1)班\"], \"homeworkId\": null, \"sendStatus\": \"success\", \"failedCount\": 0, \"notifyTitle\": \"1212\", \"receiverRole\": \"both\", \"successCount\": 6, \"homeworkTitle\": null, \"notifyContent\": \"1212\", \"receiverCount\": 6, \"notifyChannels\": [\"in_app\"]}', 0, '2026-04-10 10:33:59');
INSERT INTO `operation_log` VALUES (95007, 10002, 'teacher', 'homework_notice', 95007, 'teacher_message_send', '{\"sentAt\": \"2026-04-12 11:20\", \"classIds\": [101], \"scopeType\": \"class\", \"classNames\": [\"三年级(1)班\"], \"homeworkId\": null, \"sendStatus\": \"success\", \"failedCount\": 0, \"notifyTitle\": \"????\", \"receiverRole\": \"parent\", \"successCount\": 3, \"homeworkTitle\": null, \"notifyContent\": \"????????????\", \"receiverCount\": 3, \"notifyChannels\": [\"in_app\"]}', 0, '2026-04-12 11:20:09');
INSERT INTO `operation_log` VALUES (95008, 10002, 'teacher', 'custom_notice', 95008, 'teacher_message_send', '{\"sentAt\": \"2026-04-12 11:30\", \"classIds\": [101], \"scopeType\": \"class\", \"classNames\": [\"三年级(1)班\"], \"homeworkId\": null, \"sendStatus\": \"success\", \"failedCount\": 0, \"notifyTitle\": \"中文通知标题\", \"receiverRole\": \"parent\", \"successCount\": 3, \"homeworkTitle\": null, \"notifyContent\": \"这是一条中文通知内容\", \"receiverCount\": 3, \"notifyChannels\": [\"in_app\"]}', 0, '2026-04-12 11:30:57');
INSERT INTO `operation_log` VALUES (95009, 10002, 'teacher', 'custom_notice', 95009, 'teacher_message_send', '{\"sentAt\": \"2026-04-12 15:58\", \"classIds\": [101], \"scopeType\": \"class\", \"classNames\": [\"三年级(1)班\"], \"homeworkId\": null, \"sendStatus\": \"success\", \"failedCount\": 0, \"notifyTitle\": \"回归kind测试2\", \"receiverRole\": \"parent\", \"successCount\": 3, \"homeworkTitle\": null, \"notifyContent\": \"用于确认system kind\", \"receiverCount\": 3, \"notifyChannels\": [\"in_app\"]}', 0, '2026-04-12 15:58:17');
INSERT INTO `operation_log` VALUES (95010, 10002, 'teacher', 'custom_notice', 95010, 'teacher_message_send', '{\"sentAt\": \"2026-04-12 16:13\", \"classIds\": [101], \"scopeType\": \"class\", \"classNames\": [\"三年级(1)班\"], \"homeworkId\": null, \"sendStatus\": \"success\", \"failedCount\": 0, \"notifyTitle\": \"新版kind回归\", \"receiverRole\": \"parent\", \"successCount\": 3, \"homeworkTitle\": null, \"notifyContent\": \"检查custom_notice是否映射system\", \"receiverCount\": 3, \"notifyChannels\": [\"in_app\"]}', 0, '2026-04-12 16:13:53');

-- ----------------------------
-- Table structure for organization_class
-- ----------------------------
DROP TABLE IF EXISTS `organization_class`;
CREATE TABLE `organization_class`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `grade_id` bigint NOT NULL COMMENT '年级ID',
  `class_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级名称',
  `class_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '班级编码',
  `homeroom_teacher_id` bigint NULL DEFAULT NULL COMMENT '班主任用户ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_class_code`(`class_code` ASC) USING BTREE,
  INDEX `idx_class_school_grade`(`school_id` ASC, `grade_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 202 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '班级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of organization_class
-- ----------------------------
INSERT INTO `organization_class` VALUES (101, 1, 11, '三年级(1)班', 'C301', 10002, 'enabled', '2026-03-01 09:30:00', '2026-03-01 09:30:00');
INSERT INTO `organization_class` VALUES (102, 1, 11, '三年级(2)班', 'C302', 10003, 'enabled', '2026-03-01 09:30:00', '2026-03-01 09:30:00');
INSERT INTO `organization_class` VALUES (201, 1, 12, '四年级(1)班', 'C401', 10004, 'enabled', '2026-03-01 09:30:00', '2026-03-01 09:30:00');

-- ----------------------------
-- Table structure for organization_grade
-- ----------------------------
DROP TABLE IF EXISTS `organization_grade`;
CREATE TABLE `organization_grade`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `grade_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '年级名称',
  `school_year` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学年',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_grade_school_id`(`school_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '年级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of organization_grade
-- ----------------------------
INSERT INTO `organization_grade` VALUES (11, 1, '三年级', '2025-2026', 'enabled', '2026-03-01 08:10:00', '2026-03-01 08:10:00');
INSERT INTO `organization_grade` VALUES (12, 1, '四年级', '2025-2026', 'enabled', '2026-03-01 08:10:00', '2026-03-01 08:10:00');

-- ----------------------------
-- Table structure for organization_school
-- ----------------------------
DROP TABLE IF EXISTS `organization_school`;
CREATE TABLE `organization_school`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `school_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学校名称',
  `school_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学校编码',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_school_code`(`school_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学校表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of organization_school
-- ----------------------------
INSERT INTO `organization_school` VALUES (1, '阳光实验小学', 'SCH001', 'enabled', '2026-03-01 08:00:00', '2026-03-01 08:00:00');

-- ----------------------------
-- Table structure for parent_profile
-- ----------------------------
DROP TABLE IF EXISTS `parent_profile`;
CREATE TABLE `parent_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_user_id` bigint NOT NULL COMMENT '家长账户ID',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '家长手机号，登录标识',
  `gender` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parent_user_id`(`parent_user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_parent_mobile`(`mobile` ASC) USING BTREE,
  INDEX `idx_parent_profile_school`(`school_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '家长资料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parent_profile
-- ----------------------------
INSERT INTO `parent_profile` VALUES (42001, 30001, 1, '13710000001', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00');
INSERT INTO `parent_profile` VALUES (42002, 30002, 1, '13710000002', 'male', '2026-03-02 10:40:00', '2026-03-02 10:40:00');
INSERT INTO `parent_profile` VALUES (42003, 30003, 1, '13710000003', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00');
INSERT INTO `parent_profile` VALUES (42004, 30004, 1, '13710000004', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00');
INSERT INTO `parent_profile` VALUES (42005, 30005, 1, '13710000005', 'male', '2026-03-02 10:40:00', '2026-03-02 10:40:00');
INSERT INTO `parent_profile` VALUES (42006, 30006, 1, '13710000006', 'female', '2026-03-02 10:40:00', '2026-03-02 10:40:00');

-- ----------------------------
-- Table structure for parent_student_rel
-- ----------------------------
DROP TABLE IF EXISTS `parent_student_rel`;
CREATE TABLE `parent_student_rel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_user_id` bigint NOT NULL COMMENT '家长用户ID',
  `student_id` bigint NOT NULL COMMENT '学生档案ID',
  `relation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '关系类型',
  `is_primary` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否主联系人',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_parent_student`(`parent_user_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_parent_student_student`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '家长学生绑定关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of parent_student_rel
-- ----------------------------
INSERT INTO `parent_student_rel` VALUES (46001, 30001, 40001, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00');
INSERT INTO `parent_student_rel` VALUES (46002, 30002, 40002, 'father', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00');
INSERT INTO `parent_student_rel` VALUES (46003, 30003, 40003, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00');
INSERT INTO `parent_student_rel` VALUES (46004, 30004, 40004, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00');
INSERT INTO `parent_student_rel` VALUES (46005, 30005, 40005, 'father', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00');
INSERT INTO `parent_student_rel` VALUES (46006, 30006, 40006, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00');

-- ----------------------------
-- Table structure for student_homework_task
-- ----------------------------
DROP TABLE IF EXISTS `student_homework_task`;
CREATE TABLE `student_homework_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `student_id` bigint NOT NULL COMMENT '学生档案ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `task_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending' COMMENT '任务状态 pending submitted revision_required completed overdue',
  `latest_submission_id` bigint NULL DEFAULT NULL COMMENT '最新提交ID',
  `submission_count` int NOT NULL DEFAULT 0 COMMENT '提交次数',
  `latest_submitted_at` datetime NULL DEFAULT NULL COMMENT '最近提交时间',
  `latest_review_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'unreviewed' COMMENT '最近批改状态',
  `latest_reviewed_at` datetime NULL DEFAULT NULL COMMENT '最近批改时间',
  `is_late` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逾期',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_homework_student`(`homework_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_task_class_status`(`class_id` ASC, `task_status` ASC) USING BTREE,
  INDEX `idx_task_student`(`student_id` ASC, `task_status` ASC) USING BTREE,
  INDEX `idx_task_latest_submission`(`latest_submission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 80010 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生作业任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_homework_task
-- ----------------------------
INSERT INTO `student_homework_task` VALUES (80001, 50001, 40001, 101, 'submitted', 90007, 4, '2026-04-12 11:30:43', 'unreviewed', '2026-04-02 20:30:00', 1, 0, '2026-04-02 16:31:00', '2026-04-02 20:30:00');
INSERT INTO `student_homework_task` VALUES (80002, 50001, 40002, 101, 'revision_required', 90002, 1, '2026-04-02 19:20:00', 'revision_required', '2026-04-12 11:20:31', 0, 0, '2026-04-02 16:31:00', '2026-04-02 20:40:00');
INSERT INTO `student_homework_task` VALUES (80003, 50001, 40003, 101, 'pending', NULL, 0, NULL, 'unreviewed', NULL, 0, 0, '2026-04-02 16:31:00', '2026-04-02 16:31:00');
INSERT INTO `student_homework_task` VALUES (80004, 50002, 40004, 102, 'completed', 90003, 1, '2026-04-02 17:30:00', 'completed', '2026-04-10 16:12:39', 0, 0, '2026-04-02 09:01:00', '2026-04-02 17:30:00');
INSERT INTO `student_homework_task` VALUES (80005, 50002, 40005, 102, 'overdue', NULL, 0, NULL, 'unreviewed', NULL, 0, 0, '2026-04-02 09:01:00', '2026-04-02 18:30:00');
INSERT INTO `student_homework_task` VALUES (80006, 50003, 40006, 201, 'completed', 90004, 1, '2026-04-02 08:20:00', 'completed', '2026-04-02 09:00:00', 1, 0, '2026-03-31 17:01:00', '2026-04-02 09:00:00');

-- ----------------------------
-- Table structure for student_profile
-- ----------------------------
DROP TABLE IF EXISTS `student_profile`;
CREATE TABLE `student_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `student_user_id` bigint NOT NULL COMMENT '学生账号ID',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `grade_id` bigint NOT NULL COMMENT '年级ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `student_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学号',
  `gender` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_user_id`(`student_user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_student_school_no`(`school_id` ASC, `student_no` ASC) USING BTREE,
  INDEX `idx_student_class_id`(`class_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 40007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学生档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_profile
-- ----------------------------
INSERT INTO `student_profile` VALUES (40001, 20001, 1, 11, 101, '30101', 'female', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00');
INSERT INTO `student_profile` VALUES (40002, 20002, 1, 11, 101, '30102', 'male', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00');
INSERT INTO `student_profile` VALUES (40003, 20003, 1, 11, 101, '30103', 'female', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00');
INSERT INTO `student_profile` VALUES (40004, 20004, 1, 11, 102, '30201', 'male', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00');
INSERT INTO `student_profile` VALUES (40005, 20005, 1, 11, 102, '30202', 'female', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00');
INSERT INTO `student_profile` VALUES (40006, 20006, 1, 12, 201, '40101', 'male', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00');

-- ----------------------------
-- Table structure for teacher_class_subject_rel
-- ----------------------------
DROP TABLE IF EXISTS `teacher_class_subject_rel`;
CREATE TABLE `teacher_class_subject_rel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_id` bigint NOT NULL COMMENT '教师用户ID',
  `class_id` bigint NOT NULL COMMENT '班级ID',
  `subject_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '学科编码',
  `is_head_teacher` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否班主任',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_class_subject`(`teacher_id` ASC, `class_id` ASC, `subject_code` ASC) USING BTREE,
  INDEX `idx_teacher_class`(`class_id` ASC, `teacher_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 45006 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师班级学科关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher_class_subject_rel
-- ----------------------------
INSERT INTO `teacher_class_subject_rel` VALUES (45001, 10002, 101, 'math', 1, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00');
INSERT INTO `teacher_class_subject_rel` VALUES (45002, 10002, 101, 'chinese', 1, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00');
INSERT INTO `teacher_class_subject_rel` VALUES (45003, 10003, 102, 'chinese', 1, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00');
INSERT INTO `teacher_class_subject_rel` VALUES (45004, 10003, 102, 'math', 0, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00');
INSERT INTO `teacher_class_subject_rel` VALUES (45005, 10004, 201, 'english', 1, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00');

-- ----------------------------
-- Table structure for teacher_profile
-- ----------------------------
DROP TABLE IF EXISTS `teacher_profile`;
CREATE TABLE `teacher_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_user_id` bigint NOT NULL COMMENT '教师账户ID',
  `school_id` bigint NOT NULL COMMENT '学校ID',
  `teacher_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师工号',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '教师手机号，登录标识',
  `gender` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_teacher_user_id`(`teacher_user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_teacher_mobile`(`mobile` ASC) USING BTREE,
  UNIQUE INDEX `uk_teacher_school_no`(`school_id` ASC, `teacher_no` ASC) USING BTREE,
  INDEX `idx_teacher_profile_school`(`school_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41004 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '教师资料表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher_profile
-- ----------------------------
INSERT INTO `teacher_profile` VALUES (41001, 10002, 1, 'T301001', '13900000002', 'female', '2026-03-01 09:20:00', '2026-03-01 09:20:00');
INSERT INTO `teacher_profile` VALUES (41002, 10003, 1, 'T301002', '13900000003', 'female', '2026-03-01 09:20:00', '2026-03-01 09:20:00');
INSERT INTO `teacher_profile` VALUES (41003, 10004, 1, 'T004', '13900000004', 'female', '2026-03-01 09:20:00', '2026-04-12 11:29:49');

-- ----------------------------
-- Table structure for user_account
-- ----------------------------
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `login_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '管理员登录名，教师学生家长为空',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码密文',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `role_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色类型 teacher student parent admin',
  `school_id` bigint NULL DEFAULT NULL COMMENT '所属学校ID',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'enabled' COMMENT '状态',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_login_name`(`login_name` ASC) USING BTREE,
  INDEX `idx_user_role_school`(`role_type` ASC, `school_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统一账户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_account
-- ----------------------------
INSERT INTO `user_account` VALUES (10001, 'admin', '$2a$10$nsWUYP6fVFH2V7efKzmvUeskAnHQY276dgVveKsIvp581fwKwcwK6', '系统管理员', 'admin', 1, 'enabled', '2026-04-12 16:13:53', '2026-03-01 09:00:00', '2026-04-02 08:00:00');
INSERT INTO `user_account` VALUES (10002, NULL, '$2a$10$iyfaQySAHgEg5DtWcTIeGul9SsnH42fpt1u4tDdIw469HkQSfE892', '张丽', 'teacher', 1, 'enabled', '2026-04-12 16:13:53', '2026-03-01 09:10:00', '2026-04-07 15:46:28');
INSERT INTO `user_account` VALUES (10003, NULL, '123456', '陈晨', 'teacher', 1, 'enabled', '2026-04-10 16:09:51', '2026-03-01 09:10:00', '2026-04-07 15:46:36');
INSERT INTO `user_account` VALUES (10004, NULL, '123456', '??', 'teacher', 1, 'enabled', '2026-04-02 08:10:00', '2026-03-01 09:10:00', '2026-04-02 08:10:00');
INSERT INTO `user_account` VALUES (20001, NULL, '$2a$10$73071lYc6OQudaUTL.xNWeEgZRIoSDClrtY4vKqqsWt4zyWxVBLTe', '杨乐乐', 'student', 1, 'enabled', '2026-04-12 16:13:53', '2026-03-02 10:00:00', '2026-04-02 19:15:00');
INSERT INTO `user_account` VALUES (20002, NULL, '123456', '周子轩', 'student', 1, 'enabled', '2026-04-02 19:25:00', '2026-03-02 10:00:00', '2026-04-02 19:25:00');
INSERT INTO `user_account` VALUES (20003, NULL, '123456', '陈雨桐', 'student', 1, 'enabled', '2026-04-02 18:30:00', '2026-03-02 10:00:00', '2026-04-02 18:30:00');
INSERT INTO `user_account` VALUES (20004, NULL, '123456', '李浩然', 'student', 1, 'enabled', '2026-04-10 16:25:31', '2026-03-02 10:00:00', '2026-04-02 17:40:00');
INSERT INTO `user_account` VALUES (20005, NULL, '123456', '孙一诺', 'student', 1, 'enabled', '2026-04-02 17:00:00', '2026-03-02 10:00:00', '2026-04-02 17:00:00');
INSERT INTO `user_account` VALUES (20006, NULL, '123456', '赵思远', 'student', 1, 'enabled', '2026-04-02 08:30:00', '2026-03-02 10:00:00', '2026-04-02 08:30:00');
INSERT INTO `user_account` VALUES (30001, NULL, '$2a$10$aNfiIkDKgK08eR/J81jAcOwUtiNbcAq3THk2BIEstu.WdIxG4Wq7G', '杨乐乐妈妈', 'parent', 1, 'enabled', '2026-04-12 16:13:53', '2026-03-02 10:30:00', '2026-04-02 19:16:00');
INSERT INTO `user_account` VALUES (30002, NULL, 'z1234567', '周子轩爸爸', 'parent', 1, 'enabled', '2026-04-11 11:04:46', '2026-03-02 10:30:00', '2026-04-02 19:26:00');
INSERT INTO `user_account` VALUES (30003, NULL, '123456', '陈雨桐妈妈', 'parent', 1, 'enabled', '2026-04-02 18:31:00', '2026-03-02 10:30:00', '2026-04-02 18:31:00');
INSERT INTO `user_account` VALUES (30004, NULL, '123456', '李浩然妈妈', 'parent', 1, 'enabled', '2026-04-02 17:41:00', '2026-03-02 10:30:00', '2026-04-02 17:41:00');
INSERT INTO `user_account` VALUES (30005, NULL, '123456', '孙一诺爸爸', 'parent', 1, 'enabled', '2026-04-02 17:01:00', '2026-03-02 10:30:00', '2026-04-02 17:01:00');
INSERT INTO `user_account` VALUES (30006, NULL, '123456', '赵思远妈妈', 'parent', 1, 'enabled', '2026-04-02 08:31:00', '2026-03-02 10:30:00', '2026-04-02 08:31:00');

-- ----------------------------
-- Table structure for wrong_book_asset
-- ----------------------------
DROP TABLE IF EXISTS `wrong_book_asset`;
CREATE TABLE `wrong_book_asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `wrong_book_id` bigint NOT NULL,
  `asset_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'question_image answer_image correction_image analysis_image',
  `asset_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'image file',
  `asset_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `asset_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `sort_no` int NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wrong_book_asset_book_id`(`wrong_book_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '閿欓?鏈?檮浠惰〃' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wrong_book_asset
-- ----------------------------

-- ----------------------------
-- Table structure for wrong_book_item
-- ----------------------------
DROP TABLE IF EXISTS `wrong_book_item`;
CREATE TABLE `wrong_book_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `school_id` bigint NOT NULL,
  `student_id` bigint NOT NULL COMMENT 'student_profile.id',
  `homework_id` bigint NULL DEFAULT NULL COMMENT 'homework.id',
  `task_id` bigint NULL DEFAULT NULL COMMENT 'student_homework_task.id',
  `submission_id` bigint NULL DEFAULT NULL COMMENT 'homework_submission.id',
  `review_id` bigint NULL DEFAULT NULL COMMENT 'homework_review.id',
  `subject_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `source_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'teacher_mark student_manual system_auto',
  `question_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `question_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `student_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `correct_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `analysis_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `wrong_reason_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'pending_fix' COMMENT 'pending_fix fixed mastered',
  `added_by_user_id` bigint NOT NULL,
  `added_by_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'teacher student system',
  `recognized_confidence` decimal(5, 4) NULL DEFAULT NULL,
  `last_fixed_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `last_fixed_at` datetime NULL DEFAULT NULL,
  `fix_count` int NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wrong_book_review_question`(`review_id` ASC, `question_no` ASC, `source_type` ASC) USING BTREE,
  INDEX `idx_wrong_book_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_wrong_book_student_subject`(`student_id` ASC, `subject_code` ASC) USING BTREE,
  INDEX `idx_wrong_book_homework`(`homework_id` ASC) USING BTREE,
  INDEX `idx_wrong_book_task`(`task_id` ASC) USING BTREE,
  INDEX `idx_wrong_book_review`(`review_id` ASC) USING BTREE,
  INDEX `idx_wrong_book_source`(`source_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '閿欓?鏈?富琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wrong_book_item
-- ----------------------------
INSERT INTO `wrong_book_item` VALUES (1, 1, 40004, NULL, NULL, NULL, NULL, 'chinese', 'student_manual', '1', '1', '1', '2', '3', 'careless_error', 'pending_fix', 20004, 'student', NULL, NULL, NULL, 0, '2026-04-11 10:01:34', '2026-04-11 10:01:34');

SET FOREIGN_KEY_CHECKS = 1;
