package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

@Data
public class ClassSubjectVo {

    private String subjectCode;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
}
