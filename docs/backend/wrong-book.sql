USE `primary_homework_system`;

CREATE TABLE IF NOT EXISTS `wrong_book_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `school_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL COMMENT 'student_profile.id',
  `homework_id` BIGINT DEFAULT NULL COMMENT 'homework.id',
  `task_id` BIGINT DEFAULT NULL COMMENT 'student_homework_task.id',
  `submission_id` BIGINT DEFAULT NULL COMMENT 'homework_submission.id',
  `review_id` BIGINT DEFAULT NULL COMMENT 'homework_review.id',
  `subject_code` VARCHAR(32) NOT NULL,
  `source_type` VARCHAR(32) NOT NULL COMMENT 'teacher_mark student_manual system_auto',
  `question_no` VARCHAR(32) DEFAULT NULL,
  `question_text` TEXT DEFAULT NULL,
  `student_answer` TEXT DEFAULT NULL,
  `correct_answer` TEXT DEFAULT NULL,
  `analysis_text` TEXT DEFAULT NULL,
  `wrong_reason_code` VARCHAR(32) DEFAULT NULL,
  `status` VARCHAR(32) NOT NULL DEFAULT 'pending_fix' COMMENT 'pending_fix fixed mastered',
  `added_by_user_id` BIGINT NOT NULL,
  `added_by_role` VARCHAR(32) NOT NULL COMMENT 'teacher student system',
  `recognized_confidence` DECIMAL(5,4) DEFAULT NULL,
  `last_fixed_text` TEXT DEFAULT NULL,
  `last_fixed_at` DATETIME DEFAULT NULL,
  `fix_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wrong_book_student_status` (`student_id`, `status`),
  KEY `idx_wrong_book_student_subject` (`student_id`, `subject_code`),
  KEY `idx_wrong_book_homework` (`homework_id`),
  KEY `idx_wrong_book_task` (`task_id`),
  KEY `idx_wrong_book_review` (`review_id`),
  KEY `idx_wrong_book_source` (`source_type`),
  UNIQUE KEY `uk_wrong_book_review_question` (`review_id`, `question_no`, `source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题本主表';

CREATE TABLE IF NOT EXISTS `wrong_book_asset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `wrong_book_id` BIGINT NOT NULL,
  `asset_role` VARCHAR(32) NOT NULL COMMENT 'question_image answer_image correction_image analysis_image',
  `asset_type` VARCHAR(32) NOT NULL COMMENT 'image file',
  `asset_url` VARCHAR(512) NOT NULL,
  `asset_name` VARCHAR(255) DEFAULT NULL,
  `sort_no` INT NOT NULL DEFAULT 1,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wrong_book_asset_book_id` (`wrong_book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题本附件表';
