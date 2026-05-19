package com.primaryhomework.backend.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class HomeworkBaseDto {

    private Long homeworkId;
    private String title;
    private String subjectCode;
    private String contentText;
    private LocalDateTime deadlineAt;
    private String status;
    private Boolean allowLateSubmit;
    private Boolean allowResubmit;
    private Boolean needParentConfirm;
    private List<String> submitTypes = new ArrayList<>();
}
