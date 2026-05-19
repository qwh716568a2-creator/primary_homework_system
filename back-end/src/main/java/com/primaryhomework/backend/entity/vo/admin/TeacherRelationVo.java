package com.primaryhomework.backend.entity.vo.admin;

import lombok.Data;

@Data
public class TeacherRelationVo {

    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long classId;
    private String className;
    private String subjectCode;
    private String subjectName;
    private Boolean isHeadTeacher;
    private String status;
}
