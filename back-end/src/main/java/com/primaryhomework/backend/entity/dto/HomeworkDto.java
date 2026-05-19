package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkDto {

    private Long homeworkId;
    private String title;
    private String subjectCode;
    private String subjectName;
    private List<String> classNames = new ArrayList<>();
    private LocalDateTime deadlineAt;
    private String status;
    private Integer submittedCount;
    private Integer pendingCount;
    private Integer revisionRequiredCount;
}
