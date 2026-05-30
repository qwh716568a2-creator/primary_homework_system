USE `primary_homework_system`;

CREATE TEMPORARY TABLE IF NOT EXISTS `tmp_smart_wrong_book_seed` (
  `student_no` VARCHAR(64) NOT NULL,
  `subject_code` VARCHAR(32) NOT NULL,
  `question_no` VARCHAR(64) NOT NULL,
  `question_text` TEXT NOT NULL,
  `student_answer` TEXT,
  `correct_answer` TEXT,
  `analysis_text` TEXT,
  `wrong_reason_code` VARCHAR(64),
  `status` VARCHAR(32) NOT NULL,
  `pool_type` VARCHAR(32) NOT NULL,
  `correct_streak` INT NOT NULL,
  `mastery_score` DECIMAL(5,2) NOT NULL,
  `practice_count` INT NOT NULL,
  `last_practice_result` VARCHAR(16),
  `last_practiced_at` DATETIME,
  `fix_count` INT NOT NULL,
  `last_fixed_text` TEXT,
  `last_fixed_at` DATETIME
);

TRUNCATE TABLE `tmp_smart_wrong_book_seed`;

INSERT INTO `tmp_smart_wrong_book_seed`
(`student_no`, `subject_code`, `question_no`, `question_text`, `student_answer`, `correct_answer`, `analysis_text`, `wrong_reason_code`, `status`, `pool_type`, `correct_streak`, `mastery_score`, `practice_count`, `last_practice_result`, `last_practiced_at`, `fix_count`, `last_fixed_text`, `last_fixed_at`)
VALUES
('30201', 'math', '1', '计算：3/4 + 1/8 = ?', '4/12', '7/8', '先通分为 6/8 + 1/8，再相加得到 7/8。不能把分子分母分别相加。', 'calc_error', 'pending_fix', 'active_wrong', 0, 96.00, 0, NULL, NULL, 0, NULL, NULL),
('30201', 'math', '2', '一个长方形长 12 厘米，宽 8 厘米，周长是多少厘米？', '96 厘米', '40 厘米', '周长是两条长加两条宽，即 (12 + 8) × 2 = 40，96 是面积。', 'concept_error', 'pending_fix', 'active_wrong', 0, 92.00, 1, 'wrong', NOW() - INTERVAL 3 DAY, 0, NULL, NULL),
('30201', 'math', '3', '小明有 36 枚邮票，送给同学 1/4 后还剩多少枚？', '9 枚', '27 枚', '1/4 是送出的数量：36 × 1/4 = 9，剩下 36 - 9 = 27。', 'reading_error', 'fixed', 'active_wrong', 0, 88.00, 2, 'wrong', NOW() - INTERVAL 2 DAY, 1, '已重新画线标出“还剩”两个字，先求送出再求剩下。', NOW() - INTERVAL 2 DAY),
('30201', 'math', '4', '竖式计算：408 ÷ 6 = ?', '618', '68', '408 ÷ 6，先算 40 个十除以 6 得 6 个十余 4 个十，再落下 8，48 ÷ 6 = 8。', 'calc_error', 'pending_fix', 'active_wrong', 0, 90.00, 0, NULL, NULL, 0, NULL, NULL),
('30201', 'chinese', '5', '给“辨”和“辩”组词，并说明区别。', '辨论、争辨', '辨别、争辩', '“辨”多和分辨、辨认有关；“辩”多和说话争论有关。', 'concept_error', 'pending_fix', 'active_wrong', 0, 84.00, 1, 'wrong', NOW() - INTERVAL 5 DAY, 0, NULL, NULL),
('30201', 'chinese', '6', '修改病句：通过这次班会，使我明白了合作的重要。', '通过这次班会，使我明白了合作的重要性。', '这次班会使我明白了合作的重要。', '“通过”和“使”连用会导致句子缺少主语，应删去其中一个。', 'writing_error', 'fixed', 'active_wrong', 0, 82.00, 1, 'wrong', NOW() - INTERVAL 1 DAY, 1, '订正时删掉“通过”，保留主语“这次班会”。', NOW() - INTERVAL 1 DAY),
('30201', 'english', '7', 'Choose: She ___ to school by bus every day. A. go B. goes C. going', 'go', 'goes', '主语 She 是第三人称单数，一般现在时动词要用 goes。', 'concept_error', 'pending_fix', 'active_wrong', 0, 86.00, 0, NULL, NULL, 0, NULL, NULL),
('30201', 'english', '8', 'Write the plural form: child', 'childs', 'children', 'child 的复数是不规则变化 children，不是直接加 s。', 'concept_error', 'fixed', 'active_wrong', 0, 78.00, 2, 'wrong', NOW() - INTERVAL 4 DAY, 1, '已整理 child、foot、tooth 等不规则复数。', NOW() - INTERVAL 4 DAY),
('30201', 'math', '9', '口算：125 × 8 = ?', '1000', '1000', '最近一次答对，但这题来自历史错题，仍需再稳定一次。', 'calc_error', 'fixed', 'risky_correct', 1, 58.00, 2, 'correct', NOW() - INTERVAL 1 DAY, 1, '已掌握 125×8 的整百整千口算。', NOW() - INTERVAL 6 DAY),
('30201', 'chinese', '10', '默写：“春眠不觉晓”的下一句。', '处处闻啼鸟', '处处闻啼鸟', '最近一次默写正确，但之前常把“啼”写成“鸣”，需要巩固。', 'writing_error', 'fixed', 'risky_correct', 1, 52.00, 3, 'correct', NOW() - INTERVAL 2 DAY, 1, '已订正易错字“啼”。', NOW() - INTERVAL 7 DAY),
('30201', 'english', '11', 'Translate: 我喜欢阅读。', 'I like reading.', 'I like reading.', '最近一次答对，保留为风险正确题，观察是否连续稳定。', 'other', 'fixed', 'risky_correct', 1, 48.00, 2, 'correct', NOW() - INTERVAL 3 DAY, 1, '已能正确使用 like doing。', NOW() - INTERVAL 8 DAY),
('30201', 'math', '12', '比较大小：0.6 和 0.58', '0.6 > 0.58', '0.6 > 0.58', '已经连续稳定答对，归档为已掌握。', 'concept_error', 'mastered', 'mastered_archive', 2, 24.00, 4, 'correct', NOW() - INTERVAL 6 DAY, 2, '补零比较：0.60 > 0.58。', NOW() - INTERVAL 12 DAY),
('30201', 'chinese', '13', '给“宁静”写一个近义词。', '安静', '安静', '已经连续答对两次，归档为已掌握。', 'other', 'mastered', 'mastered_archive', 2, 20.00, 3, 'correct', NOW() - INTERVAL 5 DAY, 2, '已能区分近义词在句子中的使用。', NOW() - INTERVAL 10 DAY);

INSERT INTO `wrong_book_item`
(`school_id`, `student_id`, `homework_id`, `task_id`, `submission_id`, `review_id`, `subject_code`, `source_type`, `question_no`, `question_text`, `student_answer`, `correct_answer`, `analysis_text`, `wrong_reason_code`, `status`, `added_by_user_id`, `added_by_role`, `recognized_confidence`, `last_fixed_text`, `last_fixed_at`, `fix_count`, `pool_type`, `correct_streak`, `mastery_score`, `practice_count`, `last_practiced_at`, `last_practice_result`, `source_scene`, `created_at`, `updated_at`)
SELECT
  sp.`school_id`,
  sp.`id`,
  NULL,
  NULL,
  NULL,
  NULL,
  seed.`subject_code`,
  'student_manual',
  seed.`question_no`,
  seed.`question_text`,
  seed.`student_answer`,
  seed.`correct_answer`,
  seed.`analysis_text`,
  seed.`wrong_reason_code`,
  seed.`status`,
  sp.`student_user_id`,
  'student',
  NULL,
  seed.`last_fixed_text`,
  seed.`last_fixed_at`,
  seed.`fix_count`,
  seed.`pool_type`,
  seed.`correct_streak`,
  seed.`mastery_score`,
  seed.`practice_count`,
  seed.`last_practiced_at`,
  seed.`last_practice_result`,
  'smart_sample',
  NOW() - INTERVAL (14 - CAST(seed.`question_no` AS UNSIGNED)) DAY,
  NOW()
FROM `tmp_smart_wrong_book_seed` seed
JOIN `student_profile` sp ON sp.`student_no` = seed.`student_no`
WHERE NOT EXISTS (
  SELECT 1
  FROM `wrong_book_item` existing
  WHERE existing.`student_id` = sp.`id`
    AND existing.`source_scene` = 'smart_sample'
    AND existing.`subject_code` = seed.`subject_code`
    AND existing.`question_no` = seed.`question_no`
);

DROP TEMPORARY TABLE IF EXISTS `tmp_smart_wrong_book_seed`;
