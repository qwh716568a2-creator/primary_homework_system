USE `primary_homework_system`;

ALTER TABLE `wrong_book_item`
  ADD COLUMN `pool_type` VARCHAR(32) NOT NULL DEFAULT 'active_wrong' COMMENT 'active_wrong risky_correct mastered_archive' AFTER `fix_count`,
  ADD COLUMN `correct_streak` INT NOT NULL DEFAULT 0 AFTER `pool_type`,
  ADD COLUMN `mastery_score` DECIMAL(5,2) NOT NULL DEFAULT 100.00 AFTER `correct_streak`,
  ADD COLUMN `practice_count` INT NOT NULL DEFAULT 0 AFTER `mastery_score`,
  ADD COLUMN `last_practiced_at` DATETIME DEFAULT NULL AFTER `practice_count`,
  ADD COLUMN `last_practice_result` VARCHAR(16) DEFAULT NULL COMMENT 'correct wrong' AFTER `last_practiced_at`,
  ADD COLUMN `source_scene` VARCHAR(32) DEFAULT NULL COMMENT 'teacher_mark student_manual practice_generated' AFTER `last_practice_result`;

CREATE INDEX `idx_wrong_book_student_pool` ON `wrong_book_item` (`student_id`, `pool_type`);
CREATE INDEX `idx_wrong_book_practice_pick` ON `wrong_book_item` (`student_id`, `pool_type`, `subject_code`, `mastery_score`);

UPDATE `wrong_book_item`
SET `pool_type` = CASE WHEN `status` = 'mastered' THEN 'mastered_archive' ELSE 'active_wrong' END,
    `correct_streak` = COALESCE(`correct_streak`, 0),
    `mastery_score` = COALESCE(`mastery_score`, 100.00),
    `practice_count` = COALESCE(`practice_count`, 0),
    `source_scene` = COALESCE(`source_scene`, `source_type`)
WHERE `pool_type` IS NULL OR `source_scene` IS NULL;

CREATE TABLE IF NOT EXISTS `wrong_book_practice` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL COMMENT 'student_profile.id',
  `practice_name` VARCHAR(128) NOT NULL,
  `practice_type` VARCHAR(32) NOT NULL DEFAULT 'smart_wrong_book',
  `question_count` INT NOT NULL DEFAULT 0,
  `wrong_question_count` INT NOT NULL DEFAULT 0,
  `risky_question_count` INT NOT NULL DEFAULT 0,
  `submitted_count` INT NOT NULL DEFAULT 0,
  `correct_count` INT NOT NULL DEFAULT 0,
  `wrong_count` INT NOT NULL DEFAULT 0,
  `accuracy_rate` DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  `status` VARCHAR(32) NOT NULL DEFAULT 'generated' COMMENT 'generated in_progress completed abandoned',
  `generated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `started_at` DATETIME DEFAULT NULL,
  `submitted_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wrong_book_practice_student_time` (`student_id`, `generated_at`),
  KEY `idx_wrong_book_practice_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='smart wrong-book practice';

CREATE TABLE IF NOT EXISTS `wrong_book_practice_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `practice_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL COMMENT 'student_profile.id',
  `wrong_book_id` BIGINT NOT NULL,
  `question_no` VARCHAR(32) DEFAULT NULL,
  `subject_code` VARCHAR(32) DEFAULT NULL,
  `question_text` TEXT DEFAULT NULL,
  `correct_answer` TEXT DEFAULT NULL,
  `student_answer` TEXT DEFAULT NULL,
  `item_source_type` VARCHAR(32) NOT NULL COMMENT 'active_wrong risky_correct',
  `item_weight` DECIMAL(5,2) NOT NULL DEFAULT 100.00,
  `result_status` VARCHAR(16) NOT NULL DEFAULT 'unanswered' COMMENT 'correct wrong unanswered',
  `used_duration_seconds` INT DEFAULT NULL,
  `sort_no` INT NOT NULL DEFAULT 1,
  `submitted_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wrong_book_practice_item_practice` (`practice_id`, `sort_no`),
  KEY `idx_wrong_book_practice_item_student` (`student_id`, `practice_id`),
  KEY `idx_wrong_book_practice_item_book` (`wrong_book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='smart wrong-book practice item';
