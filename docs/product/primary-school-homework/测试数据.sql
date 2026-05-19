USE `primary_homework`;

START TRANSACTION;

-- 说明：
-- 1. 这组 SQL 默认运行在空的测试库中。
-- 2. password_hash 先使用演示值 123456，如你的登录逻辑要求加密，请后续替换为实际密文。
-- 3. 管理员使用 user_account.login_name 登录，教师使用 user_teacher.mobile 登录，
--    家长使用 user_parent.mobile 登录，学生使用 user_student.student_no 登录。
-- 4. 数据日期基于 2026-04-02 构造，包含已完成、待订正、待批改、逾期未交、逾期已交等典型状态。

INSERT INTO `school`
(`id`, `school_name`, `school_code`, `status`, `created_at`, `updated_at`)
VALUES
(1, '阳光实验小学', 'SCH001', 'enabled', '2026-03-01 08:00:00', '2026-03-01 08:00:00');

INSERT INTO `school_grade`
(`id`, `school_id`, `grade_name`, `school_year`, `status`, `created_at`, `updated_at`)
VALUES
(11, 1, '三年级', '2025-2026', 'enabled', '2026-03-01 08:10:00', '2026-03-01 08:10:00'),
(12, 1, '四年级', '2025-2026', 'enabled', '2026-03-01 08:10:00', '2026-03-01 08:10:00');

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

INSERT INTO `school_class`
(`id`, `school_id`, `grade_id`, `class_name`, `class_code`, `homeroom_teacher_id`, `status`, `created_at`, `updated_at`)
VALUES
(101, 1, 11, '三年级（1）班', 'C301', 10002, 'enabled', '2026-03-01 09:30:00', '2026-03-01 09:30:00'),
(102, 1, 11, '三年级（2）班', 'C302', 10003, 'enabled', '2026-03-01 09:30:00', '2026-03-01 09:30:00'),
(201, 1, 12, '四年级（1）班', 'C401', 10004, 'enabled', '2026-03-01 09:30:00', '2026-03-01 09:30:00');

INSERT INTO `user_student`
(`id`, `student_user_id`, `school_id`, `grade_id`, `class_id`, `student_no`, `gender`, `status`, `created_at`, `updated_at`)
VALUES
(40001, 20001, 1, 11, 101, '30101', 'female', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00'),
(40002, 20002, 1, 11, 101, '30102', 'male', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00'),
(40003, 20003, 1, 11, 101, '30103', 'female', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00'),
(40004, 20004, 1, 11, 102, '30201', 'male', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00'),
(40005, 20005, 1, 11, 102, '30202', 'female', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00'),
(40006, 20006, 1, 12, 201, '40101', 'male', 'enabled', '2026-03-02 11:00:00', '2026-03-02 11:00:00');

INSERT INTO `user_teacher_class_subject`
(`id`, `teacher_id`, `class_id`, `subject_code`, `is_head_teacher`, `status`, `created_at`, `updated_at`)
VALUES
(45001, 10002, 101, 'math', 1, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00'),
(45002, 10002, 101, 'chinese', 0, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00'),
(45003, 10003, 102, 'chinese', 1, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00'),
(45004, 10003, 102, 'math', 0, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00'),
(45005, 10004, 201, 'english', 1, 'enabled', '2026-03-03 08:00:00', '2026-03-03 08:00:00');

INSERT INTO `user_parent_student`
(`id`, `parent_user_id`, `student_id`, `relation_type`, `is_primary`, `status`, `created_at`, `updated_at`)
VALUES
(46001, 30001, 40001, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00'),
(46002, 30002, 40002, 'father', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00'),
(46003, 30003, 40003, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00'),
(46004, 30004, 40004, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00'),
(46005, 30005, 40005, 'father', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00'),
(46006, 30006, 40006, 'mother', 1, 'enabled', '2026-03-03 08:30:00', '2026-03-03 08:30:00');

INSERT INTO `homework`
(`id`, `school_id`, `creator_teacher_id`, `subject_code`, `title`, `content_text`, `deadline_at`, `allow_late_submit`, `allow_resubmit`, `submit_type_mask`, `need_parent_confirm`, `status`, `published_at`, `created_at`, `updated_at`)
VALUES
(50001, 1, 10002, 'math', '数学口算练习', '完成课后练习第1、2页，拍照上传。', '2026-04-03 20:00:00', 1, 1, 'text,image', 0, 'published', '2026-04-02 16:30:00', '2026-04-02 16:20:00', '2026-04-02 16:30:00'),
(50002, 1, 10003, 'chinese', '语文生字抄写', '抄写第3课生字每个3遍，并朗读课文。', '2026-04-02 18:00:00', 1, 1, 'text,image', 1, 'published', '2026-04-02 09:00:00', '2026-04-02 08:50:00', '2026-04-02 09:00:00'),
(50003, 1, 10004, 'english', '英语单词默写', '默写 Unit 3 单词并拍照上传。', '2026-04-01 18:00:00', 1, 1, 'image', 0, 'published', '2026-03-31 17:00:00', '2026-03-31 16:50:00', '2026-03-31 17:00:00'),
(50004, 1, 10002, 'math', '周末拓展题', '完成拓展练习单第1页，周末前完成。', '2026-04-05 20:00:00', 1, 1, 'text,file', 0, 'draft', NULL, '2026-04-02 18:00:00', '2026-04-02 18:00:00');

INSERT INTO `homework_attachment`
(`id`, `homework_id`, `asset_type`, `asset_url`, `asset_name`, `asset_size`, `sort_no`, `created_at`)
VALUES
(60001, 50001, 'file', 'https://oss.example.com/homework/20260402/math-oral.pdf', '数学口算练习.pdf', 102400, 1, '2026-04-02 16:21:00'),
(60002, 50002, 'image', 'https://oss.example.com/homework/20260402/chinese-demo.jpg', '生字示例.jpg', 204800, 1, '2026-04-02 08:55:00'),
(60003, 50003, 'file', 'https://oss.example.com/homework/20260331/english-word-list.pdf', '英语单词表.pdf', 51200, 1, '2026-03-31 16:55:00');

INSERT INTO `homework_class`
(`id`, `homework_id`, `class_id`, `created_at`)
VALUES
(70001, 50001, 101, '2026-04-02 16:30:00'),
(70002, 50002, 102, '2026-04-02 09:00:00'),
(70003, 50003, 201, '2026-03-31 17:00:00'),
(70004, 50004, 101, '2026-04-02 18:00:00');

INSERT INTO `homework_task`
(`id`, `homework_id`, `student_id`, `class_id`, `task_status`, `latest_submission_id`, `submission_count`, `latest_submitted_at`, `latest_review_status`, `latest_reviewed_at`, `is_late`, `is_deleted`, `created_at`, `updated_at`)
VALUES
(80001, 50001, 40001, 101, 'completed', 90001, 1, '2026-04-02 19:10:00', 'completed', '2026-04-02 20:30:00', 0, 0, '2026-04-02 16:31:00', '2026-04-02 20:30:00'),
(80002, 50001, 40002, 101, 'revision_required', 90002, 1, '2026-04-02 19:20:00', 'revision_required', '2026-04-02 20:40:00', 0, 0, '2026-04-02 16:31:00', '2026-04-02 20:40:00'),
(80003, 50001, 40003, 101, 'pending', NULL, 0, NULL, 'unreviewed', NULL, 0, 0, '2026-04-02 16:31:00', '2026-04-02 16:31:00'),
(80004, 50002, 40004, 102, 'submitted', 90003, 1, '2026-04-02 17:30:00', 'unreviewed', NULL, 0, 0, '2026-04-02 09:01:00', '2026-04-02 17:30:00'),
(80005, 50002, 40005, 102, 'overdue', NULL, 0, NULL, 'unreviewed', NULL, 0, 0, '2026-04-02 09:01:00', '2026-04-02 18:30:00'),
(80006, 50003, 40006, 201, 'completed', 90004, 1, '2026-04-02 08:20:00', 'completed', '2026-04-02 09:00:00', 1, 0, '2026-03-31 17:01:00', '2026-04-02 09:00:00');

INSERT INTO `homework_submission`
(`id`, `task_id`, `homework_id`, `student_id`, `operator_role`, `operator_user_id`, `submit_text`, `submitted_at`, `is_late`, `version_no`, `submit_status`, `created_at`)
VALUES
(90001, 80001, 50001, 40001, 'student', 20001, '我已完成口算练习，请老师批改。', '2026-04-02 19:10:00', 0, 1, 'submitted', '2026-04-02 19:10:00'),
(90002, 80002, 50001, 40002, 'parent', 30002, '孩子已完成，家长代为上传。', '2026-04-02 19:20:00', 0, 1, 'submitted', '2026-04-02 19:20:00'),
(90003, 80004, 50002, 40004, 'student', 20004, '生字抄写已完成。', '2026-04-02 17:30:00', 0, 1, 'submitted', '2026-04-02 17:30:00'),
(90004, 80006, 50003, 40006, 'parent', 30006, '昨天忘记提交，今天早上补交。', '2026-04-02 08:20:00', 1, 1, 'submitted', '2026-04-02 08:20:00');

INSERT INTO `homework_submission_asset`
(`id`, `submission_id`, `asset_type`, `asset_url`, `asset_name`, `asset_size`, `sort_no`, `created_at`)
VALUES
(91001, 90001, 'image', 'https://oss.example.com/submission/20260402/90001-1.jpg', '口算作业-1.jpg', 256000, 1, '2026-04-02 19:10:10'),
(91002, 90002, 'image', 'https://oss.example.com/submission/20260402/90002-1.jpg', '数学作业-周子轩.jpg', 245000, 1, '2026-04-02 19:20:10'),
(91003, 90003, 'image', 'https://oss.example.com/submission/20260402/90003-1.jpg', '语文抄写-李浩然.jpg', 268000, 1, '2026-04-02 17:30:10'),
(91004, 90004, 'image', 'https://oss.example.com/submission/20260402/90004-1.jpg', '英语默写-赵思远.jpg', 251000, 1, '2026-04-02 08:20:10');

INSERT INTO `homework_review`
(`id`, `task_id`, `homework_id`, `student_id`, `submission_id`, `reviewer_teacher_id`, `review_status`, `score`, `score_level`, `comment_text`, `reviewed_at`, `created_at`)
VALUES
(92001, 80001, 50001, 40001, 90001, 10002, 'completed', 95.00, 'A', '完成得很好，书写整洁。', '2026-04-02 20:30:00', '2026-04-02 20:30:00'),
(92002, 80002, 50001, 40002, 90002, 10002, 'revision_required', 78.00, 'B', '第2页有两道题计算错误，请订正后重新提交。', '2026-04-02 20:40:00', '2026-04-02 20:40:00'),
(92003, 80006, 50003, 40006, 90004, 10004, 'completed', 88.00, 'B', '已补交，单词拼写基本正确。', '2026-04-02 09:00:00', '2026-04-02 09:00:00');

INSERT INTO `homework_review_asset`
(`id`, `review_id`, `asset_type`, `asset_url`, `sort_no`, `created_at`)
VALUES
(93001, 92002, 'image', 'https://oss.example.com/review/20260402/92002-1.png', 1, '2026-04-02 20:40:10');

INSERT INTO `notification`
(`id`, `biz_type`, `biz_id`, `receiver_user_id`, `receiver_role`, `notify_channel`, `notify_title`, `notify_content`, `send_status`, `sent_at`, `read_at`, `created_at`)
VALUES
(94001, 'homework_publish', 50001, 20001, 'student', 'in_app', '新作业通知', '数学口算练习已发布，请于2026-04-03 20:00前完成。', 'success', '2026-04-02 16:31:00', '2026-04-02 18:50:00', '2026-04-02 16:31:00'),
(94002, 'homework_publish', 50001, 30001, 'parent', 'wechat', '新作业通知', '杨乐乐有新的数学作业，请协助提醒完成。', 'success', '2026-04-02 16:31:10', '2026-04-02 16:35:00', '2026-04-02 16:31:10'),
(94003, 'review_result', 92002, 20002, 'student', 'in_app', '批改结果通知', '数学口算练习已批改，老师要求你订正后重新提交。', 'success', '2026-04-02 20:41:00', NULL, '2026-04-02 20:41:00'),
(94004, 'review_result', 92002, 30002, 'parent', 'wechat', '批改结果通知', '周子轩的数学作业需要订正，请尽快查看。', 'success', '2026-04-02 20:41:10', NULL, '2026-04-02 20:41:10'),
(94005, 'submission_reminder', 50002, 30005, 'parent', 'wechat', '作业催交通知', '孙一诺的语文生字抄写已逾期未提交，请尽快完成。', 'success', '2026-04-02 18:20:00', NULL, '2026-04-02 18:20:00'),
(94006, 'review_result', 92003, 30006, 'parent', 'wechat', '批改结果通知', '赵思远的英语单词默写已批改，请查看老师反馈。', 'success', '2026-04-02 09:01:00', '2026-04-02 09:10:00', '2026-04-02 09:01:00');

INSERT INTO `operation_log`
(`id`, `operator_user_id`, `operator_role`, `biz_type`, `biz_id`, `action_type`, `request_payload`, `result_code`, `created_at`)
VALUES
(95001, 10002, 'teacher', 'homework', 50001, 'publish_homework', '{"title":"数学口算练习","classIds":[101],"deadlineAt":"2026-04-03 20:00:00"}', 0, '2026-04-02 16:30:00'),
(95002, 20001, 'student', 'submission', 90001, 'submit_homework', '{"taskId":80001,"assetCount":1}', 0, '2026-04-02 19:10:00'),
(95003, 30002, 'parent', 'submission', 90002, 'submit_homework_for_child', '{"taskId":80002,"studentId":40002,"assetCount":1}', 0, '2026-04-02 19:20:00'),
(95004, 10002, 'teacher', 'review', 92002, 'review_homework', '{"taskId":80002,"reviewStatus":"revision_required"}', 0, '2026-04-02 20:40:00'),
(95005, 10003, 'teacher', 'notification', 94005, 'remind_pending_homework', '{"homeworkId":50002,"classId":102,"remindType":"overdue"}', 0, '2026-04-02 18:20:00');

COMMIT;
