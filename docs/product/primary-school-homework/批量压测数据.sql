USE `primary_homework`;

START TRANSACTION;

-- 批量压测数据脚本
-- 适用前提：已执行 数据库初始化脚本.sql 和 测试数据.sql
-- 设计目标：
-- 1. 新增 40 个学生、40 个家长
-- 2. 新增 6 条教师作业，其中 5 条生成作业任务，1 条保留草稿
-- 3. 基于 101 班当前所有启用学生批量生成任务
-- 4. 在基线测试数据下，预计生成约 215 条作业任务、150+ 提交、100+ 批改
-- 5. 可重复执行，脚本会先清理本脚本生成的数据

SET @teacher_user_id := 10002;
SET @school_id := 1;
SET @grade_id := 11;
SET @class_id := 101;

-- 清理旧的批量压测数据
DELETE FROM `homework_review_asset`
WHERE `review_id` IN (
  SELECT `id` FROM `homework_review` WHERE `homework_id` BETWEEN 51001 AND 51006
);

DELETE FROM `homework_review` WHERE `homework_id` BETWEEN 51001 AND 51006;

DELETE FROM `homework_submission_asset`
WHERE `submission_id` IN (
  SELECT `id` FROM `homework_submission` WHERE `homework_id` BETWEEN 51001 AND 51006
);

DELETE FROM `homework_submission` WHERE `homework_id` BETWEEN 51001 AND 51006;
DELETE FROM `homework_task` WHERE `homework_id` BETWEEN 51001 AND 51006;
DELETE FROM `homework_class` WHERE `homework_id` BETWEEN 51001 AND 51006;
DELETE FROM `homework_attachment` WHERE `homework_id` BETWEEN 51001 AND 51006;
DELETE FROM `operation_log` WHERE `id` BETWEEN 951001 AND 951050;
DELETE FROM `notification` WHERE `id` BETWEEN 941001 AND 941500;
DELETE FROM `homework` WHERE `id` BETWEEN 51001 AND 51006;

DELETE FROM `user_parent_student` WHERE `id` BETWEEN 461001 AND 461040;
DELETE FROM `user_parent` WHERE `id` BETWEEN 421001 AND 421040;
DELETE FROM `user_student` WHERE `id` BETWEEN 401001 AND 401040;
DELETE FROM `user_account`
WHERE (`id` BETWEEN 210001 AND 210040)
   OR (`id` BETWEEN 310001 AND 310040);

DROP TEMPORARY TABLE IF EXISTS `tmp_digits`;
DROP TEMPORARY TABLE IF EXISTS `tmp_bulk_students`;
DROP TEMPORARY TABLE IF EXISTS `tmp_bulk_homeworks`;
DROP TEMPORARY TABLE IF EXISTS `tmp_bulk_tasks`;
DROP TEMPORARY TABLE IF EXISTS `tmp_bulk_submissions`;
DROP TEMPORARY TABLE IF EXISTS `tmp_bulk_reviews`;

CREATE TEMPORARY TABLE `tmp_digits` (
  `n` TINYINT NOT NULL PRIMARY KEY
);

INSERT INTO `tmp_digits` (`n`)
VALUES (0), (1), (2), (3), (4), (5), (6), (7), (8), (9);

CREATE TEMPORARY TABLE `tmp_bulk_students` AS
SELECT
  `seq`,
  210000 + `seq` AS `student_user_id`,
  310000 + `seq` AS `parent_user_id`,
  401000 + `seq` AS `student_id`,
  421000 + `seq` AS `parent_id`,
  461000 + `seq` AS `relation_id`,
  @school_id AS `school_id`,
  @grade_id AS `grade_id`,
  @class_id AS `class_id`,
  CONCAT('35', LPAD(`seq`, 3, '0')) AS `student_no`,
  CASE WHEN MOD(`seq`, 2) = 1 THEN 'male' ELSE 'female' END AS `gender`,
  CONCAT('压测学生', LPAD(`seq`, 2, '0')) AS `student_name`,
  CONCAT('压测家长', LPAD(`seq`, 2, '0')) AS `parent_name`,
  CONCAT('1373', LPAD(`seq`, 7, '0')) AS `mobile`,
  DATE_ADD('2026-04-08 08:00:00', INTERVAL `seq` MINUTE) AS `created_at`
FROM (
  SELECT (t.`n` * 10 + o.`n` + 1) AS `seq`
  FROM `tmp_digits` o
  CROSS JOIN `tmp_digits` t
  WHERE (t.`n` * 10 + o.`n`) < 40
  ORDER BY `seq`
) s;

INSERT INTO `user_account`
(`id`, `login_name`, `password_hash`, `user_name`, `role_type`, `school_id`, `status`, `last_login_at`, `created_at`, `updated_at`)
SELECT
  `student_user_id`,
  NULL,
  '123456',
  `student_name`,
  'student',
  `school_id`,
  'enabled',
  DATE_ADD('2026-04-08 18:00:00', INTERVAL `seq` MINUTE),
  `created_at`,
  `created_at`
FROM `tmp_bulk_students`;

INSERT INTO `user_account`
(`id`, `login_name`, `password_hash`, `user_name`, `role_type`, `school_id`, `status`, `last_login_at`, `created_at`, `updated_at`)
SELECT
  `parent_user_id`,
  NULL,
  '123456',
  `parent_name`,
  'parent',
  `school_id`,
  'enabled',
  DATE_ADD('2026-04-08 18:30:00', INTERVAL `seq` MINUTE),
  `created_at`,
  `created_at`
FROM `tmp_bulk_students`;

INSERT INTO `user_student`
(`id`, `student_user_id`, `school_id`, `grade_id`, `class_id`, `student_no`, `gender`, `status`, `created_at`, `updated_at`)
SELECT
  `student_id`,
  `student_user_id`,
  `school_id`,
  `grade_id`,
  `class_id`,
  `student_no`,
  `gender`,
  'enabled',
  `created_at`,
  `created_at`
FROM `tmp_bulk_students`;

INSERT INTO `user_parent`
(`id`, `parent_user_id`, `school_id`, `mobile`, `gender`, `created_at`, `updated_at`)
SELECT
  `parent_id`,
  `parent_user_id`,
  `school_id`,
  `mobile`,
  `gender`,
  `created_at`,
  `created_at`
FROM `tmp_bulk_students`;

INSERT INTO `user_parent_student`
(`id`, `parent_user_id`, `student_id`, `relation_type`, `is_primary`, `status`, `created_at`, `updated_at`)
SELECT
  `relation_id`,
  `parent_user_id`,
  `student_id`,
  CASE WHEN MOD(`seq`, 3) = 0 THEN 'father' ELSE 'mother' END,
  1,
  'enabled',
  `created_at`,
  `created_at`
FROM `tmp_bulk_students`;

CREATE TEMPORARY TABLE `tmp_bulk_homeworks` (
  `homework_id` BIGINT NOT NULL PRIMARY KEY,
  `subject_code` VARCHAR(32) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `content_text` TEXT NULL,
  `deadline_at` DATETIME NOT NULL,
  `allow_late_submit` TINYINT(1) NOT NULL,
  `allow_resubmit` TINYINT(1) NOT NULL,
  `submit_type_mask` VARCHAR(64) NOT NULL,
  `need_parent_confirm` TINYINT(1) NOT NULL,
  `status` VARCHAR(32) NOT NULL,
  `published_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NOT NULL
);

INSERT INTO `tmp_bulk_homeworks`
(`homework_id`, `subject_code`, `title`, `content_text`, `deadline_at`, `allow_late_submit`, `allow_resubmit`, `submit_type_mask`, `need_parent_confirm`, `status`, `published_at`, `created_at`, `updated_at`)
VALUES
(51001, 'math', '数学压测作业 A', '批量压测：口算、填空、应用题混合练习。', '2026-04-10 20:00:00', 1, 1, 'text,image', 0, 'published', '2026-04-08 09:00:00', '2026-04-08 08:40:00', '2026-04-08 09:00:00'),
(51002, 'chinese', '语文压测作业 B', '批量压测：阅读短文并完成答题卡。', '2026-04-04 18:00:00', 0, 0, 'text,image', 1, 'published', '2026-04-03 09:00:00', '2026-04-03 08:40:00', '2026-04-04 18:30:00'),
(51003, 'math', '数学压测作业 C', '批量压测：周练计算题与思维题。', '2026-04-11 20:00:00', 1, 1, 'image,file', 0, 'published', '2026-04-08 10:00:00', '2026-04-08 09:45:00', '2026-04-08 10:00:00'),
(51004, 'chinese', '语文压测草稿 D', '批量压测：预习任务草稿，不生成任务。', '2026-04-12 20:00:00', 1, 1, 'text,file', 0, 'draft', NULL, '2026-04-08 10:30:00', '2026-04-08 10:30:00'),
(51005, 'math', '数学压测作业 E', '批量压测：错题回炉作业，后续撤回。', '2026-04-05 18:00:00', 1, 0, 'image', 0, 'revoked', '2026-04-02 16:00:00', '2026-04-02 15:30:00', '2026-04-06 08:30:00'),
(51006, 'math', '数学压测作业 F', '批量压测：课堂达标小测。', '2026-04-09 12:00:00', 1, 0, 'image', 0, 'published', '2026-04-08 15:00:00', '2026-04-08 14:40:00', '2026-04-08 15:00:00');

INSERT INTO `homework`
(`id`, `school_id`, `creator_teacher_id`, `subject_code`, `title`, `content_text`, `deadline_at`, `allow_late_submit`, `allow_resubmit`, `submit_type_mask`, `need_parent_confirm`, `status`, `published_at`, `created_at`, `updated_at`)
SELECT
  `homework_id`,
  @school_id,
  @teacher_user_id,
  `subject_code`,
  `title`,
  `content_text`,
  `deadline_at`,
  `allow_late_submit`,
  `allow_resubmit`,
  `submit_type_mask`,
  `need_parent_confirm`,
  `status`,
  `published_at`,
  `created_at`,
  `updated_at`
FROM `tmp_bulk_homeworks`;

INSERT INTO `homework_attachment`
(`id`, `homework_id`, `asset_type`, `asset_url`, `asset_name`, `asset_size`, `sort_no`, `created_at`)
VALUES
(610001, 51001, 'file', 'https://oss.example.com/stress/homework/51001-guide.pdf', '数学压测作业A-说明.pdf', 102400, 1, '2026-04-08 08:41:00'),
(610002, 51002, 'file', 'https://oss.example.com/stress/homework/51002-reading-card.pdf', '语文压测作业B-阅读卡.pdf', 86400, 1, '2026-04-03 08:41:00'),
(610003, 51003, 'file', 'https://oss.example.com/stress/homework/51003-sheet.pdf', '数学压测作业C-题单.pdf', 128000, 1, '2026-04-08 09:46:00'),
(610004, 51004, 'file', 'https://oss.example.com/stress/homework/51004-preview.docx', '语文压测草稿D-预习清单.docx', 56320, 1, '2026-04-08 10:31:00'),
(610005, 51005, 'file', 'https://oss.example.com/stress/homework/51005-rework.pdf', '数学压测作业E-补做卷.pdf', 118000, 1, '2026-04-02 15:31:00'),
(610006, 51006, 'file', 'https://oss.example.com/stress/homework/51006-quiz.pdf', '数学压测作业F-小测题单.pdf', 90500, 1, '2026-04-08 14:41:00');

INSERT INTO `homework_class`
(`id`, `homework_id`, `class_id`, `created_at`)
VALUES
(710001, 51001, @class_id, '2026-04-08 09:00:00'),
(710002, 51002, @class_id, '2026-04-03 09:00:00'),
(710003, 51003, @class_id, '2026-04-08 10:00:00'),
(710004, 51004, @class_id, '2026-04-08 10:30:00'),
(710005, 51005, @class_id, '2026-04-02 16:00:00'),
(710006, 51006, @class_id, '2026-04-08 15:00:00');

CREATE TEMPORARY TABLE `tmp_bulk_tasks` AS
SELECT
  810000 + `num`.`seq` AS `task_id`,
  `num`.`homework_id`,
  `num`.`student_id`,
  `num`.`class_id`,
  CASE
    WHEN MOD(`num`.`seq`, 10) IN (1, 2, 3) THEN 'pending'
    WHEN MOD(`num`.`seq`, 10) IN (4, 5) THEN 'submitted'
    WHEN MOD(`num`.`seq`, 10) IN (6, 7, 8) THEN 'completed'
    ELSE 'revision_required'
  END AS `task_status`,
  CASE
    WHEN MOD(`num`.`seq`, 10) IN (4, 5, 6, 7, 8, 9, 0) THEN 1 ELSE 0
  END AS `has_submission`,
  CASE
    WHEN MOD(`num`.`seq`, 10) IN (6, 7, 8, 9, 0) THEN 1 ELSE 0
  END AS `has_review`,
  CASE
    WHEN `num`.`homework_id` IN (51002, 51005)
     AND MOD(`num`.`seq`, 10) IN (4, 5, 6, 7, 8, 9, 0)
     AND MOD(`num`.`seq`, 4) = 0 THEN 1
    ELSE 0
  END AS `is_late`,
  DATE_ADD(DATE_SUB(`num`.`deadline_at`, INTERVAL 1 DAY), INTERVAL MOD(`num`.`seq`, 180) MINUTE) AS `created_at`
FROM (
  SELECT
    (@task_seq := @task_seq + 1) AS `seq`,
    `seed`.`homework_id`,
    `seed`.`deadline_at`,
    `seed`.`student_id`,
    `seed`.`class_id`
  FROM (
    SELECT
      `h`.`homework_id`,
      `h`.`deadline_at`,
      `s`.`id` AS `student_id`,
      `s`.`class_id`
    FROM `tmp_bulk_homeworks` h
    JOIN `user_student` s
      ON `s`.`class_id` = @class_id
     AND `s`.`status` = 'enabled'
    WHERE `h`.`status` <> 'draft'
    ORDER BY `h`.`homework_id`, `s`.`id`
  ) `seed`
  CROSS JOIN (SELECT @task_seq := 0) `vars`
) `num`;

CREATE TEMPORARY TABLE `tmp_bulk_submissions` AS
SELECT
  910000 + `num`.`seq` AS `submission_id`,
  `num`.`task_id`,
  `num`.`homework_id`,
  `num`.`student_id`,
  CASE WHEN MOD(`num`.`seq`, 4) = 0 THEN 'parent' ELSE 'student' END AS `operator_role`,
  CASE WHEN MOD(`num`.`seq`, 4) = 0 THEN `num`.`parent_user_id` ELSE `num`.`student_user_id` END AS `operator_user_id`,
  CASE
    WHEN `num`.`task_status` = 'revision_required' THEN '已完成当前版本，请老师指出需要继续修改的地方。'
    WHEN `num`.`task_status` = 'completed' THEN '题目已全部完成，请老师批改。'
    ELSE '批量压测提交内容。'
  END AS `submit_text`,
  CASE
    WHEN `num`.`homework_id` = 51002 AND `num`.`is_late` = 1 THEN DATE_ADD('2026-04-05 19:00:00', INTERVAL `num`.`seq` MINUTE)
    WHEN `num`.`homework_id` = 51002 THEN DATE_ADD('2026-04-04 16:00:00', INTERVAL MOD(`num`.`seq`, 120) MINUTE)
    WHEN `num`.`homework_id` = 51005 AND `num`.`is_late` = 1 THEN DATE_ADD('2026-04-06 09:00:00', INTERVAL `num`.`seq` MINUTE)
    WHEN `num`.`homework_id` = 51005 THEN DATE_ADD('2026-04-05 16:00:00', INTERVAL MOD(`num`.`seq`, 120) MINUTE)
    ELSE DATE_ADD('2026-04-08 18:00:00', INTERVAL `num`.`seq` MINUTE)
  END AS `submitted_at`,
  `num`.`is_late`
FROM (
  SELECT
    (@sub_seq := @sub_seq + 1) AS `seq`,
    `seed`.`task_id`,
    `seed`.`homework_id`,
    `seed`.`student_id`,
    `seed`.`task_status`,
    `seed`.`is_late`,
    `us`.`student_user_id`,
    COALESCE(`ups`.`parent_user_id`, `us`.`student_user_id`) AS `parent_user_id`
  FROM (
    SELECT `task_id`, `homework_id`, `student_id`, `task_status`, `is_late`
    FROM `tmp_bulk_tasks`
    WHERE `has_submission` = 1
    ORDER BY `task_id`
  ) `seed`
  JOIN `user_student` `us` ON `us`.`id` = `seed`.`student_id`
  LEFT JOIN `user_parent_student` `ups`
    ON `ups`.`student_id` = `seed`.`student_id`
   AND `ups`.`status` = 'enabled'
   AND `ups`.`is_primary` = 1
  CROSS JOIN (SELECT @sub_seq := 0) `vars`
) `num`;

CREATE TEMPORARY TABLE `tmp_bulk_reviews` AS
SELECT
  920000 + `num`.`seq` AS `review_id`,
  `num`.`task_id`,
  `num`.`homework_id`,
  `num`.`student_id`,
  `num`.`submission_id`,
  @teacher_user_id AS `reviewer_teacher_id`,
  CASE WHEN `num`.`task_status` = 'revision_required' THEN 'revision_required' ELSE 'completed' END AS `review_status`,
  CASE
    WHEN `num`.`task_status` = 'revision_required' THEN 72 + MOD(`num`.`seq`, 10)
    ELSE 88 + MOD(`num`.`seq`, 10)
  END AS `score`,
  CASE
    WHEN `num`.`task_status` = 'revision_required' THEN 'B'
    WHEN MOD(`num`.`seq`, 3) = 0 THEN 'S'
    ELSE 'A'
  END AS `score_level`,
  CASE
    WHEN `num`.`task_status` = 'revision_required' THEN '仍有部分步骤不完整，请根据批注继续订正。'
    ELSE '答案完整，书写清晰，继续保持。'
  END AS `comment_text`,
  DATE_ADD(`num`.`submitted_at`, INTERVAL 20 + MOD(`num`.`seq`, 40) MINUTE) AS `reviewed_at`
FROM (
  SELECT
    (@review_seq := @review_seq + 1) AS `seq`,
    `t`.`task_id`,
    `t`.`homework_id`,
    `t`.`student_id`,
    `t`.`task_status`,
    `s`.`submission_id`,
    `s`.`submitted_at`
  FROM (
    SELECT `task_id`, `homework_id`, `student_id`, `task_status`
    FROM `tmp_bulk_tasks`
    WHERE `has_review` = 1
    ORDER BY `task_id`
  ) `t`
  JOIN `tmp_bulk_submissions` `s` ON `s`.`task_id` = `t`.`task_id`
  CROSS JOIN (SELECT @review_seq := 0) `vars`
) `num`;

INSERT INTO `homework_task`
(`id`, `homework_id`, `student_id`, `class_id`, `task_status`, `latest_submission_id`, `submission_count`, `latest_submitted_at`, `latest_review_status`, `latest_reviewed_at`, `is_late`, `is_deleted`, `created_at`, `updated_at`)
SELECT
  `t`.`task_id`,
  `t`.`homework_id`,
  `t`.`student_id`,
  `t`.`class_id`,
  `t`.`task_status`,
  `s`.`submission_id`,
  CASE WHEN `s`.`submission_id` IS NULL THEN 0 ELSE 1 END,
  `s`.`submitted_at`,
  COALESCE(`r`.`review_status`, 'unreviewed'),
  `r`.`reviewed_at`,
  CASE WHEN `s`.`submission_id` IS NULL THEN 0 ELSE `t`.`is_late` END,
  0,
  `t`.`created_at`,
  COALESCE(`r`.`reviewed_at`, `s`.`submitted_at`, `t`.`created_at`)
FROM `tmp_bulk_tasks` `t`
LEFT JOIN `tmp_bulk_submissions` `s` ON `s`.`task_id` = `t`.`task_id`
LEFT JOIN `tmp_bulk_reviews` `r` ON `r`.`task_id` = `t`.`task_id`;

INSERT INTO `homework_submission`
(`id`, `task_id`, `homework_id`, `student_id`, `operator_role`, `operator_user_id`, `submit_text`, `submitted_at`, `is_late`, `version_no`, `submit_status`, `created_at`)
SELECT
  `submission_id`,
  `task_id`,
  `homework_id`,
  `student_id`,
  `operator_role`,
  `operator_user_id`,
  `submit_text`,
  `submitted_at`,
  `is_late`,
  1,
  'submitted',
  `submitted_at`
FROM `tmp_bulk_submissions`;

INSERT INTO `homework_submission_asset`
(`id`, `submission_id`, `asset_type`, `asset_url`, `asset_name`, `asset_size`, `sort_no`, `created_at`)
SELECT
  911000 + `s`.`submission_id` - 910000,
  `s`.`submission_id`,
  'image',
  CONCAT('https://oss.example.com/stress/submission/', `s`.`submission_id`, '.jpg'),
  CONCAT('压测提交-', `s`.`submission_id`, '.jpg'),
  204800 + MOD(`s`.`submission_id`, 20) * 1024,
  1,
  `s`.`submitted_at`
FROM `tmp_bulk_submissions` `s`;

INSERT INTO `homework_review`
(`id`, `task_id`, `homework_id`, `student_id`, `submission_id`, `reviewer_teacher_id`, `review_status`, `score`, `score_level`, `comment_text`, `reviewed_at`, `created_at`)
SELECT
  `review_id`,
  `task_id`,
  `homework_id`,
  `student_id`,
  `submission_id`,
  `reviewer_teacher_id`,
  `review_status`,
  `score`,
  `score_level`,
  `comment_text`,
  `reviewed_at`,
  `reviewed_at`
FROM `tmp_bulk_reviews`;

INSERT INTO `homework_review_asset`
(`id`, `review_id`, `asset_type`, `asset_url`, `sort_no`, `created_at`)
SELECT
  930000 + `review_id` - 920000,
  `review_id`,
  'image',
  CONCAT('https://oss.example.com/stress/review/', `review_id`, '.png'),
  1,
  `reviewed_at`
FROM `tmp_bulk_reviews`
WHERE `review_status` = 'revision_required';

INSERT INTO `notification`
(`id`, `biz_type`, `biz_id`, `receiver_user_id`, `receiver_role`, `notify_channel`, `notify_title`, `notify_content`, `send_status`, `sent_at`, `read_at`, `created_at`)
SELECT
  941000 + `num`.`seq`,
  CASE WHEN `num`.`category` = 'publish' THEN 'homework_publish' ELSE 'review_result' END,
  `num`.`biz_id`,
  `num`.`receiver_user_id`,
  `num`.`receiver_role`,
  CASE WHEN `num`.`receiver_role` = 'parent' THEN 'wechat' ELSE 'in_app' END,
  CASE WHEN `num`.`category` = 'publish' THEN '批量压测作业通知' ELSE '批量压测批改通知' END,
  `num`.`content_text`,
  'success',
  `num`.`event_time`,
  NULL,
  `num`.`event_time`
FROM (
  SELECT
    (@notify_seq := @notify_seq + 1) AS `seq`,
    'publish' AS `category`,
    `h`.`homework_id` AS `biz_id`,
    `u`.`student_user_id` AS `receiver_user_id`,
    'student' AS `receiver_role`,
    CONCAT(`h`.`title`, ' 已发布，请及时完成。') AS `content_text`,
    DATE_ADD(`h`.`published_at`, INTERVAL MOD(`u`.`seq`, 15) MINUTE) AS `event_time`
  FROM `tmp_bulk_homeworks` `h`
  JOIN `tmp_bulk_students` `u`
    ON `h`.`status` = 'published'
   AND `u`.`seq` <= 10
  CROSS JOIN (SELECT @notify_seq := 0) `vars1`
  UNION ALL
  SELECT
    100 + (@notify_seq2 := @notify_seq2 + 1) AS `seq`,
    'review' AS `category`,
    `r`.`review_id` AS `biz_id`,
    `us`.`student_user_id` AS `receiver_user_id`,
    'student' AS `receiver_role`,
    CASE WHEN `r`.`review_status` = 'revision_required' THEN '你的作业需要继续订正。' ELSE '你的作业已批改完成。' END AS `content_text`,
    `r`.`reviewed_at` AS `event_time`
  FROM `tmp_bulk_reviews` `r`
  JOIN `user_student` `us` ON `us`.`id` = `r`.`student_id`
  CROSS JOIN (SELECT @notify_seq2 := 0) `vars2`
  WHERE `r`.`review_id` <= 920020
) `num`;

INSERT INTO `operation_log`
(`id`, `operator_user_id`, `operator_role`, `biz_type`, `biz_id`, `action_type`, `request_payload`, `result_code`, `created_at`)
SELECT
  951000 + `row_num`,
  @teacher_user_id,
  'teacher',
  'homework',
  `homework_id`,
  CASE
    WHEN `status` = 'draft' THEN 'save_draft_homework'
    WHEN `status` = 'revoked' THEN 'revoke_homework'
    ELSE 'publish_homework'
  END,
  CONCAT('{"homeworkId":', `homework_id`, ',"title":"', `title`, '","status":"', `status`, '"}'),
  0,
  `updated_at`
FROM (
  SELECT
    (@log_seq := @log_seq + 1) AS `row_num`,
    `homework_id`,
    `title`,
    `status`,
    `updated_at`
  FROM `tmp_bulk_homeworks`
  CROSS JOIN (SELECT @log_seq := 0) `vars`
  ORDER BY `homework_id`
) `logs`;

COMMIT;
