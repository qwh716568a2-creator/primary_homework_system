package com.primaryhomework.backend.entity.dto;

import lombok.Data;

@Data
public class ClassDto {

    private Long classId;
    private String className;
    private String subjectCode;
    private String subjectName;
    private Boolean isHeadTeacher;
}
