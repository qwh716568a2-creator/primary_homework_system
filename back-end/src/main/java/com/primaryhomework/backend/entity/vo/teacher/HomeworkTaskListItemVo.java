package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomeworkTaskListItemVo {

    private Long taskId;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private String taskStatus;
    private String reviewStatus;
    private LocalDateTime latestSubmittedAt;
    private Integer submissionCount;
    private Boolean isLate;
}
