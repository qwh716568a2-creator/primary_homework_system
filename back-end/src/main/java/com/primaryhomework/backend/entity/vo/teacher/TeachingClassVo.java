package com.primaryhomework.backend.entity.vo.teacher;

import lombok.Data;

@Data
public class TeachingClassVo {

    private Long relationId;
    private Long classId;
    private String className;
    private Long gradeId;
    private String gradeName;
    private Long schoolId;
    private String schoolName;
    private String subjectCode;
    private String subjectName;
    private Integer studentCount;
    private Boolean isHeadTeacher;
    private String status;
}
