USE `primary_homework_system`;

CREATE TABLE IF NOT EXISTS `mobile_preference` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_type` VARCHAR(32) NOT NULL COMMENT 'student parent',
  `master_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `assignment_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `review_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `reminder_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `system_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `sound_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `vibration_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `quiet_hours_enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `quiet_start` VARCHAR(8) NOT NULL DEFAULT '22:00',
  `quiet_end` VARCHAR(8) NOT NULL DEFAULT '07:00',
  `hide_account_identifier` TINYINT(1) NOT NULL DEFAULT 0,
  `remember_account` TINYINT(1) NOT NULL DEFAULT 1,
  `login_alert_enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `app_lock_enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `biometric_enabled` TINYINT(1) NOT NULL DEFAULT 0,
  `password_checked_at` DATETIME DEFAULT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mobile_preference_user_role` (`user_id`, `role_type`),
  KEY `idx_mobile_preference_role` (`role_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='小程序消息设置与账号安全设置';
