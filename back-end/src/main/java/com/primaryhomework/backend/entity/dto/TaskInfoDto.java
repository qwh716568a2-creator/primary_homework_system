package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class TaskInfoDto {

    private Long taskId;
    private Long studentId;
    private String studentName;
    private String taskStatus;
    private String reviewStatus;
}
