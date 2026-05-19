package com.primaryhomework.backend.entity.vo.admin;

import lombok.Data;

@Data
public class AdminClassVo {

    private Long classId;
    private Long schoolId;
    private String schoolName;
    private Long gradeId;
    private String gradeName;
    private String className;
    private String classCode;
    private Long homeroomTeacherId;
    private String homeroomTeacherName;
    private Integer studentCount;
    private String status;
}
