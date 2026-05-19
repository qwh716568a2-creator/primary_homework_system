package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class HomeworkClassDto {

    private Long classId;
    private String className;
    private Integer studentCount;
    private Integer submittedCount;
    private Integer completedCount;
    private Integer revisionRequiredCount;
    private Integer overdueCount;
}
