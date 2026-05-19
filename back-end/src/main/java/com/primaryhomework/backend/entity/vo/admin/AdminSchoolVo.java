package com.primaryhomework.backend.entity.vo.admin;

import lombok.Data;

@Data
public class AdminSchoolVo {

    private Long schoolId;
    private String schoolName;
    private String schoolCode;
    private String status;
    private Integer gradeCount;
    private Integer classCount;
    private Integer teacherCount;
    private Integer studentCount;
}
