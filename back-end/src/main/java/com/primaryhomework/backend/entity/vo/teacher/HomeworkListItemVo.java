package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkListItemVo {

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
