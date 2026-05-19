ALTER TABLE `user_teacher_class_subject`
ADD UNIQUE KEY `uk_class_subject` (`class_id`, `subject_code`);
